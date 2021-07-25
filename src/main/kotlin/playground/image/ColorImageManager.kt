package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkFormatProperties
import playground.ApplicationState

fun createColorImage(appState: ApplicationState) {
    stackPush().use { stack ->
        appState.colorImage = createImage(
            appState.swapchainWidth!!, appState.swapchainHeight!!, appState.swapchainColorFormat!!, appState.sampleCount!!,
            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, "color", appState.device, stack
        )
    }
}

fun createColorImageView(appState: ApplicationState) {
    stackPush().use { stack ->
        appState.colorImageView = createImageView(
            appState.colorImage!!, appState.swapchainColorFormat!!, VK_IMAGE_ASPECT_COLOR_BIT, "color", appState.device, stack
        )
    }
}

fun destroyColorImageView(appState: ApplicationState) {
    if (appState.colorImageView != null) {
        vkDestroyImageView(appState.device, appState.colorImageView!!, null)
    }
}

fun destroyColorImage(appState: ApplicationState) {
    if (appState.colorImage != null) {
        vkDestroyImage(appState.device, appState.colorImage!!, null)
    }
}
