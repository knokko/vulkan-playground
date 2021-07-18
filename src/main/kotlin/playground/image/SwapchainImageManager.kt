package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkImageViewCreateInfo
import playground.ApplicationState
import playground.assertSuccess

fun createSwapchainImageViews(appState: ApplicationState) {
    stackPush().use { stack ->
        for (swapchainImage in appState.swapchainImages) {
            swapchainImage.view = createImageView(
                swapchainImage.image, appState.swapchainColorFormat!!, VK_IMAGE_ASPECT_COLOR_BIT, "swapchain", appState.device, stack
            )
        }
    }
}

fun destroySwapchainImageViews(appState: ApplicationState) {
    appState.destroySwapchainImageViews()
}
