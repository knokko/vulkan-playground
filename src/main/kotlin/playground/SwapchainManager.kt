package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
import org.lwjgl.vulkan.VkSurfaceFormatKHR
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR
import org.lwjgl.vulkan.VK10.*

fun createSwapchain(appState: ApplicationState) {
    stackPush().use {stack ->

        val surfaceCaps = VkSurfaceCapabilitiesKHR.callocStack(stack)
        assertSuccess(
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(appState.physicalDevice, appState.windowSurface!!, surfaceCaps),
            "GetPhysicalDeviceSurfaceCapabilitiesKHR"
        )

        var imageCount = surfaceCaps.minImageCount() + 1
        if (surfaceCaps.maxImageCount() in 1 until imageCount) {
            imageCount = surfaceCaps.maxImageCount()
        }

        val pNumFormats = stack.callocInt(1)
        assertSuccess(
            vkGetPhysicalDeviceSurfaceFormatsKHR(appState.physicalDevice, appState.windowSurface!!, pNumFormats, null),
            "GetPhysicalDeviceSurfaceFormatsKHR", "count"
        )
        val numFormats = pNumFormats[0]

        val formats = VkSurfaceFormatKHR.callocStack(numFormats, stack)
        assertSuccess(
            vkGetPhysicalDeviceSurfaceFormatsKHR(appState.physicalDevice, appState.windowSurface!!, pNumFormats, formats),
            "GetPhysicalDeviceSurfaceFormatsKHR", "formats"
        )

        val bestImageFormats = arrayOf(VK_FORMAT_B8G8R8_SRGB, VK_FORMAT_R8G8B8_SRGB)
        val niceImageFormats = arrayOf(VK_FORMAT_B8G8R8A8_SRGB, VK_FORMAT_R8G8B8A8_SRGB)

        // Use the first format as back-up if we find nothing nice
        var chosenFormat = formats[0]
        println("There are $numFormats available surface formats:")
        for (format in formats) {
            println("colorSpace is ${format.colorSpace()} and imageFormat is ${format.format()}")
            // This is currently the only color space not behind an extension. Also, the spec guarantees we won't need
            // a custom shader to transform it
            if (format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                if (bestImageFormats.contains(format.format())) {
                    chosenFormat = format
                    break
                } else if (niceImageFormats.contains(format.format())) {
                    chosenFormat = format
                }
            }
        }
        println()
        println("Chose (colorSpace, imageFormat) = (${chosenFormat.colorSpace()}, ${chosenFormat.format()})")
        appState.swapchainColorFormat = chosenFormat.format()

        val swapchainExtent = surfaceCaps.currentExtent()
        // -1 is a magic value indicating that the surface extent is not yet known and will depend on swapchain extent
        if (swapchainExtent.width() == -1) {
            val (width, height) = getWindowSize(appState, stack)
            swapchainExtent.set(width, height)
        }
        appState.swapchainWidth = swapchainExtent.width()
        appState.swapchainHeight = swapchainExtent.height()

        val ciSwapchain = VkSwapchainCreateInfoKHR.callocStack(stack)
        ciSwapchain.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
        ciSwapchain.surface(appState.windowSurface!!)
        ciSwapchain.minImageCount(imageCount)
        ciSwapchain.imageFormat(chosenFormat.format())
        ciSwapchain.imageColorSpace(chosenFormat.colorSpace())
        ciSwapchain.imageExtent(swapchainExtent)
        ciSwapchain.imageArrayLayers(1)
        ciSwapchain.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
        ciSwapchain.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
        ciSwapchain.preTransform(surfaceCaps.currentTransform())
        ciSwapchain.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
        ciSwapchain.presentMode(VK_PRESENT_MODE_FIFO_KHR)
        ciSwapchain.clipped(true)
        ciSwapchain.oldSwapchain(VK_NULL_HANDLE)

        val pSwapchain = stack.callocLong(1)
        assertSuccess(
            vkCreateSwapchainKHR(appState.device, ciSwapchain, null, pSwapchain),
            "CreateSwapchainKHR"
        )
        appState.swapchain = pSwapchain[0]

        val pNumImages = stack.callocInt(1)
        assertSuccess(
            vkGetSwapchainImagesKHR(appState.device, appState.swapchain!!, pNumImages, null),
            "GetSwapchainImagesKHR", "count"
        )
        val numImages = pNumImages[0]

        val images = stack.callocLong(numImages)
        assertSuccess(
            vkGetSwapchainImagesKHR(appState.device, appState.swapchain!!, pNumImages, images),
            "GetSwapchainImagesKHR", "images"
        )

        val imagesArray = Array(numImages) {index -> SwapchainImage(images[index])}
        appState.swapchainImages = imagesArray
    }
}

fun destroySwapchain(appState: ApplicationState) {
    if (appState.swapchain != null) {
        vkDestroySwapchainKHR(appState.device, appState.swapchain!!, null)
    }
}
