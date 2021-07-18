package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import playground.ApplicationState
import playground.assertSuccess

fun createDepthImage(appState: ApplicationState) {
    stackPush().use {stack ->

        val desiredDepthFormats = arrayOf(
            // The 24-bit depth formats are preferred because they are less computationally expensive
            // https://developer.nvidia.com/blog/vulkan-dos-donts/ recommends not to use 32-bit depth formats
            VK_FORMAT_X8_D24_UNORM_PACK32, VK_FORMAT_D24_UNORM_S8_UINT,
            VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT
        )

        val formatProps = VkFormatProperties.callocStack(stack)
        for (depthFormat in desiredDepthFormats) {
            vkGetPhysicalDeviceFormatProperties(appState.physicalDevice, depthFormat, formatProps)
            if ((formatProps.optimalTilingFeatures() and VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0) {
                appState.depthFormat = depthFormat
                break
            }
        }
        println("Chose depth format ${appState.depthFormat}")

        appState.depthImage = createImage(
            appState.swapchainWidth!!, appState.swapchainHeight!!,
            appState.depthFormat!!, appState.sampleCount!!, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
            "depth", appState.device, stack
        )
    }
}

fun createDepthImageView(appState: ApplicationState) {
    stackPush().use { stack ->
        appState.depthImageView = createImageView(
            appState.depthImage!!, appState.depthFormat!!, VK_IMAGE_ASPECT_DEPTH_BIT, "depth", appState.device, stack
        )
    }
}

fun destroyDepthImage(appState: ApplicationState) {
    if (appState.depthImage != null) {
        vkDestroyImage(appState.device, appState.depthImage!!, null)
    }
}

fun destroyDepthImageView(appState: ApplicationState) {
    if (appState.depthImageView != null) {
        vkDestroyImageView(appState.device, appState.depthImageView!!, null)
    }
}
