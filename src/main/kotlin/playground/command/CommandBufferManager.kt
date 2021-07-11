package playground.command

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import playground.ApplicationState
import playground.assertSuccess

fun createStaticDrawCommandBuffers(appState: ApplicationState) {
    stackPush().use { stack ->
        val aiDrawCommand = VkCommandBufferAllocateInfo.callocStack(stack)
        aiDrawCommand.commandPool(appState.staticDrawCommandPool!!)
        aiDrawCommand.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
        aiDrawCommand.commandBufferCount(appState.framebuffers.size)

        val pCommandBuffers = stack.callocPointer(appState.framebuffers.size)
        assertSuccess(
            vkAllocateCommandBuffers(appState.device, aiDrawCommand, pCommandBuffers),
            "AllocateCommandBuffers", "static draw"
        )
        val drawBuffers = Array(appState.framebuffers.size) { index -> VkCommandBuffer(pCommandBuffers[index], appState.device) }

        for (index in 0 until appState.framebuffers.size) {
            val drawBuffer = drawBuffers[index]

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
            biRenderPass.framebuffer(appState.framebuffers[index])
            // renderArea.offset will stay (0, 0)
            biRenderPass.renderArea().extent().set(appState.swapchainWidth!!, appState.swapchainHeight!!)
            biRenderPass.pClearValues(clearColors)

            vkCmdBeginRenderPass(drawBuffer, biRenderPass, VK_SUBPASS_CONTENTS_INLINE)

            // TODO The rest of the command buffer
        }
    }
}
