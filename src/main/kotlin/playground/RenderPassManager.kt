package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkAttachmentDescription
import org.lwjgl.vulkan.VkAttachmentReference
import org.lwjgl.vulkan.VkRenderPassCreateInfo
import org.lwjgl.vulkan.VkSubpassDescription

fun createRenderPasses(appState: ApplicationState) {
    createBasicRenderPass(appState)
}

fun createBasicRenderPass(appState: ApplicationState) {
    stackPush().use {stack ->
        val attachments = VkAttachmentDescription.callocStack(3, stack)

        val colorAttachment = attachments[0]
        colorAttachment.format(appState.swapchainColorFormat!!)
        colorAttachment.samples(appState.sampleCount!!)
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

        val depthAttachment = attachments[1]
        depthAttachment.format(appState.depthFormat!!)
        depthAttachment.samples(appState.sampleCount!!)
        depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
        depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
        depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)

        val resolveAttachment = attachments[2]
        resolveAttachment.format(appState.swapchainColorFormat!!)
        resolveAttachment.samples(VK_SAMPLE_COUNT_1_BIT)
        resolveAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
        resolveAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
        resolveAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        resolveAttachment.finalLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)

        val refColorAttachments = VkAttachmentReference.callocStack(1, stack)
        val refColorAttachment = refColorAttachments[0]
        refColorAttachment.attachment(0)
        refColorAttachment.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

        val refDepthAttachment = VkAttachmentReference.callocStack(stack)
        refDepthAttachment.attachment(1)
        refDepthAttachment.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)

        val refResolveAttachments = VkAttachmentReference.callocStack(1, stack)
        val refResolveAttachment = refResolveAttachments[0]
        refResolveAttachment.attachment(2)
        refResolveAttachment.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

        val subpasses = VkSubpassDescription.callocStack(1, stack)
        // Just 1 subpass
        val subpass = subpasses[0]
        subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
        subpass.colorAttachmentCount(1)
        subpass.pColorAttachments(refColorAttachments)
        subpass.pDepthStencilAttachment(refDepthAttachment)
        subpass.pResolveAttachments(refResolveAttachments)
        // No input, resolve, and preserve attachments

        val ciRenderPass = VkRenderPassCreateInfo.callocStack(stack)
        ciRenderPass.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
        ciRenderPass.pAttachments(attachments)
        ciRenderPass.pSubpasses(subpasses)
        // No dependencies

        val pRenderPass = stack.callocLong(1)
        assertSuccess(
            vkCreateRenderPass(appState.device, ciRenderPass, null, pRenderPass),
            "CreateRenderPass", "basic"
        )
        appState.basicRenderPass = pRenderPass[0]
    }
}

fun destroyRenderPasses(appState: ApplicationState) {
    if (appState.basicRenderPass != null) {
        vkDestroyRenderPass(appState.device, appState.basicRenderPass!!, null)
    }
}
