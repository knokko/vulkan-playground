package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAddress
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*

fun drawFrame(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciPresentFence = VkFenceCreateInfo.callocStack(stack)
        ciPresentFence.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)

        val pPresentFence = stack.callocLong(1)
        assertSuccess(
            vkCreateFence(appState.device, ciPresentFence, null, pPresentFence),
            "CreateFence", "present finished"
        )
        val presentFinishedFence = pPresentFence[0]

        // TODO Handle swapchain out-of-date
        val pNextImageIndex = stack.callocInt(1)
        assertSuccess(
            vkAcquireNextImageKHR(
                appState.device, appState.swapchain!!, 1_000_000_000, VK_NULL_HANDLE, presentFinishedFence, pNextImageIndex),
            "AcquireNextImageKHR"
        )
        val imageIndex = pNextImageIndex[0]
        val swapchainImage = appState.swapchainImages[imageIndex]

        val submissions = VkSubmitInfo.callocStack(1, stack)
        val siDraw = submissions[0]
        siDraw.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        siDraw.pCommandBuffers(stack.pointers(swapchainImage.staticDrawCommandBuffer))
        siDraw.pSignalSemaphores(stack.longs(swapchainImage.renderSemaphore))

        fillDrawingBuffers(appState)

        // For some reason, we have to wait for both the presentFence and renderFence, otherwise the validation layers
        // will spit errors claiming that the command buffer isn't finished yet. That happens even if we use Thread.sleep
        // to wait for 500 milliseconds... It is also weird since presenting is always done after rendering (semaphores
        // ensure that), so it shouldn't matter whether we only wait on presentFence or on both presentFence and renderFence.
        assertSuccess(
            vkWaitForFences(appState.device, stack.longs(pPresentFence[0], swapchainImage.renderFence), true, 1_000_000_000),
            "WaitForFences", "present signal"
        )
        assertSuccess(
            vkResetFences(appState.device, stack.longs(swapchainImage.renderFence)),
            "ResetFences", "render"
        )

        assertSuccess(
            vkQueueSubmit(appState.graphicsQueue, submissions, swapchainImage.renderFence),
            "QueueSubmit", "draw static"
        )

        val presentInfo = VkPresentInfoKHR.callocStack(stack)
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
        presentInfo.pWaitSemaphores(stack.longs(swapchainImage.renderSemaphore))
        presentInfo.swapchainCount(1)
        presentInfo.pSwapchains(stack.longs(appState.swapchain!!))
        presentInfo.pImageIndices(pNextImageIndex)

        assertSuccess(
            vkQueuePresentKHR(appState.graphicsQueue, presentInfo),
            "QueuePresentKHR"
        )

        vkDestroyFence(appState.device, presentFinishedFence, null)
    }
}

fun fillDrawingBuffers(appState: ApplicationState) {
    // TODO Well... fill the drawing buffers
    val numDrawCalls = 1
    appState.indirectDrawData.putInt(appState.indirectCountOffset!!, numDrawCalls)

    val drawCommands = VkDrawIndexedIndirectCommand.create(memAddress(appState.indirectDrawData) + appState.indirectDrawOffset!!, MAX_INDIRECT_DRAW_COUNT)
    val drawCommand = drawCommands[0]
    drawCommand.indexCount(6)
    drawCommand.instanceCount(1)
    drawCommand.firstIndex(0)
    drawCommand.vertexOffset(0)
    drawCommand.firstInstance(0)

    // TODO Also flush storage and uniform buffer
    stackPush().use { stack ->
        val memoryRanges = VkMappedMemoryRange.callocStack(1, stack)

        val drawMemoryRange = memoryRanges[0]
        drawMemoryRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        drawMemoryRange.memory(appState.indirectMemory!!)
        // This could be optimized by flushing only the parts that are used, but be careful with nonCoherentAtomSize
        drawMemoryRange.offset(0)
        drawMemoryRange.size(VK_WHOLE_SIZE)

        assertSuccess(
            vkFlushMappedMemoryRanges(appState.device, memoryRanges),
            "FlushMappedMemoryRanges", "draw frame"
        )
    }
}
