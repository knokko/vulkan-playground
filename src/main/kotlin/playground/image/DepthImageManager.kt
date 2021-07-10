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

        val ciImage = VkImageCreateInfo.callocStack(stack)
        ciImage.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
        ciImage.imageType(VK_IMAGE_TYPE_2D)
        ciImage.format(appState.depthFormat!!)
        ciImage.extent().width(appState.swapchainWidth!!)
        ciImage.extent().height(appState.swapchainHeight!!)
        ciImage.extent().depth(1)
        ciImage.mipLevels(1)
        ciImage.arrayLayers(1)
        ciImage.samples(appState.sampleCount)
        ciImage.tiling(VK_IMAGE_TILING_OPTIMAL)
        ciImage.usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
        ciImage.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
        ciImage.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)

        val pImage = stack.callocLong(1)
        assertSuccess(
            vkCreateImage(appState.device, ciImage, null, pImage),
            "CreateImage", "depth"
        )
        appState.depthImage = pImage[0]
    }
}

fun createDepthImageView(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciImageView = VkImageViewCreateInfo.callocStack(stack)
        ciImageView.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
        ciImageView.image(appState.depthImage!!)
        ciImageView.viewType(VK_IMAGE_VIEW_TYPE_2D)
        ciImageView.format(appState.depthFormat!!)
        ciImageView.components().set(
            VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY,
            VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY
        )
        ciImageView.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
        ciImageView.subresourceRange().baseMipLevel(0)
        ciImageView.subresourceRange().levelCount(1)
        ciImageView.subresourceRange().baseArrayLayer(0)
        ciImageView.subresourceRange().layerCount(1)

        val pImageView = stack.callocLong(1)
        assertSuccess(
            vkCreateImageView(appState.device, ciImageView, null, pImageView),
            "CreateImageView", "depth"
        )
        appState.depthImageView = pImageView[0]
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
