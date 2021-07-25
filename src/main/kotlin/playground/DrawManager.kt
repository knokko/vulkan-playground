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

        val drawSubmissions = VkSubmitInfo.callocStack(1, stack)
        val siDraw = drawSubmissions[0]
        siDraw.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        siDraw.pCommandBuffers(stack.pointers(swapchainImage.staticDrawCommandBuffer))
        siDraw.pSignalSemaphores(stack.longs(swapchainImage.renderSemaphore))

        val preparePresentSubmissions = VkSubmitInfo.callocStack(1, stack)
        val siPreparePresent = preparePresentSubmissions[0]
        siPreparePresent.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        siPreparePresent.pCommandBuffers(stack.pointers(swapchainImage.preparePresentCommandBuffer))
        siPreparePresent.waitSemaphoreCount(1)
        siPreparePresent.pWaitSemaphores(stack.longs(swapchainImage.renderSemaphore))
        siPreparePresent.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
        siPreparePresent.pSignalSemaphores(stack.longs(swapchainImage.preparePresentSemaphore))

        // Before rewriting the drawing buffers, we should wait until the current rendering is finished because the
        // GPU may still be reading from these buffers.
        assertSuccess(
            vkWaitForFences(appState.device, stack.longs(appState.renderFence!!), true, 1_000_000_000),
            "WaitForFences", "shared render"
        )
        assertSuccess(
            vkResetFences(appState.device, stack.longs(appState.renderFence!!)),
            "ResetFences", "shared render"
        )
        fillDrawingBuffers(appState)

        assertSuccess(
            vkWaitForFences(appState.device, pPresentFence, true, 1_000_000_000),
            "WaitForFences", "present signal"
        )

        assertSuccess(
            vkQueueSubmit(appState.graphicsQueue, drawSubmissions, appState.renderFence!!),
            "QueueSubmit", "draw static"
        )

        if (appState.useVR!!) {
            submitSwapchainImageToVr(appState, swapchainImage)
        }

        assertSuccess(
            vkQueueSubmit(appState.graphicsQueue, preparePresentSubmissions, VK_NULL_HANDLE),
            "QueueSubmit", "prepare present"
        )

        val presentInfo = VkPresentInfoKHR.callocStack(stack)
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
        presentInfo.pWaitSemaphores(stack.longs(swapchainImage.preparePresentSemaphore))
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
    val numDrawCalls = 3
    appState.indirectDrawData.putInt(appState.indirectCountOffset!!, numDrawCalls)

    val drawCommands = VkDrawIndexedIndirectCommand.create(
        memAddress(appState.indirectDrawData) + appState.indirectDrawOffset!!,
        MAX_INDIRECT_DRAW_COUNT
    )

    val terrain5 = appState.terrainModels.model5
    val terrain15 = appState.terrainModels.model15
    val terrain50 = appState.terrainModels.model50

    val drawCommand1 = drawCommands[0]
    drawCommand1.indexCount(terrain5.numIndices)
    drawCommand1.instanceCount(1)
    drawCommand1.firstIndex(terrain5.indexOffset)
    drawCommand1.vertexOffset(terrain5.vertexOffset)
    drawCommand1.firstInstance(0)

    val drawCommand2 = drawCommands[1]
    drawCommand2.indexCount(terrain15.numIndices)
    drawCommand2.instanceCount(1)
    drawCommand2.firstIndex(terrain15.indexOffset)
    drawCommand2.vertexOffset(terrain15.vertexOffset)
    drawCommand2.firstInstance(1)

    val drawCommand3 = drawCommands[2]
    drawCommand3.indexCount(terrain50.numIndices)
    drawCommand3.instanceCount(1)
    drawCommand3.firstIndex(terrain50.indexOffset)
    drawCommand3.vertexOffset(terrain50.vertexOffset)
    drawCommand3.firstInstance(2)

    val cameraMatrix = run {
        val projectionMatrix = Matrix4f().scale(1f, -1f, 1f).perspective(
            toRadians(70f),
            appState.swapchainWidth!!.toFloat() / appState.swapchainHeight!!.toFloat(),
            0.01f, 1000f, true
        )

        val viewMatrix = Matrix4f()
            .rotateXYZ(toRadians(appState.camera.pitch), toRadians(appState.camera.yaw), 0f)
            .translate(-appState.camera.posX, -appState.camera.posY, -appState.camera.posZ)
        projectionMatrix.mul(viewMatrix)
    }

    cameraMatrix.get(appState.uniformData)

    val offsetCameraPosition = 4 * 4 * 4
    appState.uniformData.putFloat(offsetCameraPosition, appState.camera.posX)
    appState.uniformData.putFloat(offsetCameraPosition + 4, appState.camera.posY)
    appState.uniformData.putFloat(offsetCameraPosition + 8, appState.camera.posZ)

    val transformationMatrix1 = Matrix4f().scale(100f).translate(-1.6f, 0f, -0.5f)
    transformationMatrix1.get(appState.storageData)

    val transformationMatrix2 = Matrix4f().scale(100f).translate(-0.5f, 0f, -0.5f)
    transformationMatrix2.get(64, appState.storageData)

    val transformationMatrix3 = Matrix4f().scale(100f).translate(0.6f, 0f, -0.5f)
    transformationMatrix3.get(128, appState.storageData)

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

fun createRenderFence(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciRenderFence = VkFenceCreateInfo.callocStack(stack)
        ciRenderFence.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
        ciRenderFence.flags(VK_FENCE_CREATE_SIGNALED_BIT)

        val pRenderFence = stack.callocLong(1)
        assertSuccess(
            vkCreateFence(appState.device, ciRenderFence, null, pRenderFence),
            "CreateFence", "shared rendering"
        )
        appState.renderFence = pRenderFence[0]
    }
}

fun destroyRenderFence(appState: ApplicationState) {
    if (appState.renderFence != null) {
        vkDestroyFence(appState.device, appState.renderFence!!, null)
    }
}
