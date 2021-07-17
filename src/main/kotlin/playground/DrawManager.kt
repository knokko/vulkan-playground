package playground

import org.joml.Math.toRadians
import org.joml.Matrix4f
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
    val numDrawCalls = 2
    appState.indirectDrawData.putInt(appState.indirectCountOffset!!, numDrawCalls)

    val drawCommands = VkDrawIndexedIndirectCommand.create(
        memAddress(appState.indirectDrawData) + appState.indirectDrawOffset!!,
        MAX_INDIRECT_DRAW_COUNT
    )

    val terrain5 = appState.terrainModels.model50
    //println("terrain5 is (${terrain5.vertexOffset}, ${terrain5.numVertices}, ${terrain5.indexOffset}, ${terrain5.numIndices}")

    val drawCommand1 = drawCommands[0]
    drawCommand1.indexCount(terrain5.numIndices)
    drawCommand1.instanceCount(1)
    drawCommand1.firstIndex(terrain5.indexOffset)
    drawCommand1.vertexOffset(terrain5.vertexOffset)
    drawCommand1.firstInstance(0)

    val drawCommand2 = drawCommands[1]
    drawCommand2.indexCount(6)
    drawCommand2.instanceCount(1)
    drawCommand2.firstIndex(0)
    drawCommand2.vertexOffset(0)
    drawCommand2.firstInstance(1)

    val cameraMatrix = run {
        val projectionMatrix = Matrix4f().setPerspective(
            toRadians(70f),
            appState.swapchainWidth!!.toFloat() / appState.swapchainHeight!!.toFloat(),
            0.01f, 1000f, true
        )

        val viewMatrix = Matrix4f().rotateXYZ(toRadians(80f), 0f, 0f).translate(0f, -80f, 0f)
        projectionMatrix.mul(viewMatrix)
    }

    cameraMatrix.get(appState.uniformData)

    val transformationMatrix1 = Matrix4f().scale(100f).translate(-0.5f, 0f, -0.5f)
    transformationMatrix1.get(appState.storageData)

    val transformationMatrix2 = Matrix4f().translate(10.2f, 0f, -60f)
    transformationMatrix2.get(64, appState.storageData)

    stackPush().use { stack ->
        val memoryRanges = VkMappedMemoryRange.callocStack(3, stack)

        val drawMemoryRange = memoryRanges[0]
        drawMemoryRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        drawMemoryRange.memory(appState.indirectMemory!!)
        // This could be optimized by flushing only the parts that are used, but be careful with nonCoherentAtomSize
        drawMemoryRange.offset(0)
        drawMemoryRange.size(VK_WHOLE_SIZE)

        val uniformMemoryRange = memoryRanges[1]
        uniformMemoryRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        uniformMemoryRange.memory(appState.uniformMemory!!)
        uniformMemoryRange.offset(0)
        // Currently, the entire uniform memory must be written in each frame
        uniformMemoryRange.size(VK_WHOLE_SIZE)

        val storageMemoryRange = memoryRanges[2]
        storageMemoryRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        storageMemoryRange.memory(appState.storageMemory!!)
        storageMemoryRange.offset(0)
        // This could be optimized by flushing only the parts that are used, but be careful with nonCoherentAtomSize
        storageMemoryRange.size(VK_WHOLE_SIZE)

        assertSuccess(
            vkFlushMappedMemoryRanges(appState.device, memoryRanges),
            "FlushMappedMemoryRanges", "draw frame"
        )
    }
}
