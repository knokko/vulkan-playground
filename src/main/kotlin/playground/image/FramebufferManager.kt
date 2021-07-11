package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO
import org.lwjgl.vulkan.VK10.vkCreateFramebuffer
import org.lwjgl.vulkan.VkFramebufferCreateInfo
import playground.ApplicationState
import playground.assertSuccess

fun createFramebuffers(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciFramebuffer = VkFramebufferCreateInfo.callocStack(stack)
        ciFramebuffer.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
        ciFramebuffer.renderPass(appState.basicRenderPass!!)
        // attachments will be assigned in the for loop
        ciFramebuffer.width(appState.swapchainWidth!!)
        ciFramebuffer.height(appState.swapchainHeight!!)
        ciFramebuffer.layers(1)

        val framebuffers = Array(appState.swapchainImages.size) { 0L }

        for (imageIndex in 0 until appState.swapchainImages.size) {
            // We always use the same depth image because we do at most 1 rendering operation at a time
            ciFramebuffer.pAttachments(stack.longs(appState.swapchainImageViews[imageIndex], appState.depthImageView!!))

            val pFramebuffer = stack.callocLong(1)
            assertSuccess(
                vkCreateFramebuffer(appState.device, ciFramebuffer, null, pFramebuffer),
                "CreateFramebuffer", "$imageIndex"
            )
            framebuffers[imageIndex] = pFramebuffer[0]
        }

        appState.framebuffers = framebuffers
    }
}

fun destroyFramebuffers(appState: ApplicationState) {
    appState.destroyFramebuffers()
}
