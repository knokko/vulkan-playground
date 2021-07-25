package playground.command

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRDrawIndirectCount.vkCmdDrawIndexedIndirectCountKHR
import org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
import org.lwjgl.vulkan.VK10.*
import playground.ApplicationState
import playground.INDIRECT_DRAW_STRIDE
import playground.MAX_INDIRECT_DRAW_COUNT
import playground.assertSuccess
import playground.vertex.INDEX_TYPE

fun createStaticDrawCommandBuffers(appState: ApplicationState) {
    stackPush().use { stack ->
        val aiDrawCommand = VkCommandBufferAllocateInfo.callocStack(stack)
        aiDrawCommand.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
        aiDrawCommand.commandPool(appState.staticDrawCommandPool!!)
        aiDrawCommand.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
        aiDrawCommand.commandBufferCount(appState.swapchainImages.size)

        val pCommandBuffers = stack.callocPointer(appState.swapchainImages.size)
        assertSuccess(
            vkAllocateCommandBuffers(appState.device, aiDrawCommand, pCommandBuffers),
            "AllocateCommandBuffers", "static draw"
        )
        for ((index, swapchainImage) in appState.swapchainImages.withIndex()) {
            swapchainImage.staticDrawCommandBuffer = VkCommandBuffer(pCommandBuffers[index], appState.device)
        }

        val viewports = VkViewport.callocStack(1, stack)
        val viewport = viewports[0]
        // viewport.x and viewport.y will stay 0
        viewport.width(appState.swapchainWidth!!.toFloat())
        viewport.height(appState.swapchainHeight!!.toFloat())
        viewport.minDepth(0f)
        viewport.maxDepth(1f)

        val scissors = VkRect2D.callocStack(1, stack)
        // scissor.offset will stay 0
        scissors[0].extent().set(appState.swapchainWidth!!, appState.swapchainHeight!!)

        for (swapchainImage in appState.swapchainImages) {
            val drawBuffer = swapchainImage.staticDrawCommandBuffer

            val biDrawCommand = VkCommandBufferBeginInfo.callocStack(stack)
            biDrawCommand.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)

            assertSuccess(
                vkBeginCommandBuffer(drawBuffer, biDrawCommand),
                "BeginCommandBuffer", "static draw"
            )

            val clearColors = VkClearValue.callocStack(2, stack)
            val clearColor = clearColors[0].color().float32()
            clearColor.put(0, 1f)
            clearColor.put(1, 0.5f)
            clearColor.put(2, 1f)
            clearColor.put(3, 1f)
            clearColors[1].depthStencil().depth(1f)

            val biRenderPass = VkRenderPassBeginInfo.callocStack(stack)
            biRenderPass.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
            biRenderPass.renderPass(appState.basicRenderPass!!)
            biRenderPass.framebuffer(swapchainImage.framebuffer!!)
            // renderArea.offset will stay (0, 0)
            biRenderPass.renderArea().extent().set(appState.swapchainWidth!!, appState.swapchainHeight!!)
            biRenderPass.pClearValues(clearColors)

            vkCmdBeginRenderPass(drawBuffer, biRenderPass, VK_SUBPASS_CONTENTS_INLINE)
            vkCmdBindPipeline(drawBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, appState.basicPipeline!!)
            vkCmdSetViewport(drawBuffer, 0, viewports)
            vkCmdSetScissor(drawBuffer, 0, scissors)
            vkCmdBindVertexBuffers(drawBuffer, 0, stack.longs(appState.vertexBuffer!!), stack.longs(0))
            vkCmdBindIndexBuffer(drawBuffer, appState.indexBuffer!!, 0, INDEX_TYPE)
            vkCmdBindDescriptorSets(
                drawBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                appState.basicPipelineLayout!!,
                0,
                stack.longs(appState.basicDescriptorSet!!),
                null
            )
            vkCmdDrawIndexedIndirectCountKHR(
                drawBuffer,
                appState.indirectDrawBuffer!!,
                appState.indirectDrawOffset!!,
                appState.indirectDrawBuffer!!,
                appState.indirectCountOffset!!.toLong(),
                MAX_INDIRECT_DRAW_COUNT,
                INDIRECT_DRAW_STRIDE
            )
            vkCmdEndRenderPass(drawBuffer)
            vkEndCommandBuffer(drawBuffer)
        }
    }
}

fun createPreparePresentCommandBuffers(appState: ApplicationState) {
    stackPush().use { stack ->
        val numCommandBuffers = appState.swapchainImages.size

        val aiPrepareCommand = VkCommandBufferAllocateInfo.callocStack(stack)
        aiPrepareCommand.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
        aiPrepareCommand.commandPool(appState.staticDrawCommandPool!!)
        aiPrepareCommand.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
        aiPrepareCommand.commandBufferCount(numCommandBuffers)

        val pPrepareCommands = stack.callocPointer(numCommandBuffers)
        assertSuccess(
            vkAllocateCommandBuffers(appState.device, aiPrepareCommand, pPrepareCommands),
            "AllocateCommandBuffers", "prepare present"
        )

        for ((index, swapchainImage) in appState.swapchainImages.withIndex()) {
            val prepareCommand = VkCommandBuffer(pPrepareCommands[index], appState.device)

            val biPrepare = VkCommandBufferBeginInfo.callocStack(stack)
            biPrepare.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)

            assertSuccess(
                vkBeginCommandBuffer(prepareCommand, biPrepare),
                "BeginCommandBuffer", "prepare present"
            )

            val transitions = VkImageMemoryBarrier.callocStack(1, stack)
            val transition = transitions[0]
            transition.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
            transition.srcAccessMask(0)
            transition.dstAccessMask(0)
            transition.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
            transition.newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
            transition.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
            transition.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
            transition.image(swapchainImage.image)
            transition.subresourceRange { srr ->
                srr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                srr.baseMipLevel(0)
                srr.levelCount(1)
                srr.baseArrayLayer(0)
                srr.layerCount(1)
            }

            vkCmdPipelineBarrier(
                prepareCommand, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
                0, null, null, transitions
            )

            assertSuccess(
                vkEndCommandBuffer(prepareCommand), "EndCommandBuffer", "prepare present"
            )

            swapchainImage.preparePresentCommandBuffer = prepareCommand
        }
    }
}

fun destroyPreparePresentCommandBuffers(appState: ApplicationState) {
    appState.destroyPreparePresentCommandBuffers()
}

fun destroyStaticDrawCommandBuffers(appState: ApplicationState) {
    appState.destroyStaticDrawCommandBuffers()
}
