package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkImageViewCreateInfo
import playground.ApplicationState
import playground.assertSuccess

fun createSwapchainImageViews(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciView = VkImageViewCreateInfo.callocStack(stack)
        ciView.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
        // image will be assigned in the next loop
        ciView.viewType(VK_IMAGE_VIEW_TYPE_2D)
        ciView.format(appState.swapchainColorFormat!!)
        ciView.components().set(
            VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY,
            VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY
        )
        val sr = ciView.subresourceRange()
        sr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
        sr.levelCount(1)
        sr.baseMipLevel(0)
        sr.layerCount(1)
        sr.baseArrayLayer(0)

        val views = Array(appState.swapchainImages.size) { 0L }

        for ((index, swapchainImage) in appState.swapchainImages.withIndex()) {
            ciView.image(swapchainImage)

            val pView = stack.callocLong(1)
            assertSuccess(
                vkCreateImageView(appState.device, ciView, null, pView),
                "CreateImageView", "swapchain $index"
            )
            views[index] = pView[0]
        }

        appState.swapchainImageViews = views
    }
}

fun destroySwapchainImageViews(appState: ApplicationState) {
    appState.destroySwapchainImageViews()
}