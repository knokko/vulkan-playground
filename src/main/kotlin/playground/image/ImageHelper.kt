package playground.image

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkImageCreateInfo
import org.lwjgl.vulkan.VkImageViewCreateInfo
import playground.assertSuccess

fun createImage(
    width: Int, height: Int, format: Int, sampleCount: Int, usage: Int,
    description: String, device: VkDevice, stack: MemoryStack
): Long {
    val ciImage = VkImageCreateInfo.callocStack(stack)
    ciImage.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
    ciImage.imageType(VK_IMAGE_TYPE_2D)
    ciImage.format(format)
    ciImage.extent().width(width)
    ciImage.extent().height(height)
    ciImage.extent().depth(1)
    ciImage.mipLevels(1)
    ciImage.arrayLayers(1)
    ciImage.samples(sampleCount)
    ciImage.tiling(VK_IMAGE_TILING_OPTIMAL)
    ciImage.usage(usage)
    ciImage.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
    ciImage.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)

    val pImage = stack.callocLong(1)
    assertSuccess(
        vkCreateImage(device, ciImage, null, pImage),
        "CreateImage", description
    )
    return pImage[0]
}

fun createImageView(image: Long, format: Int, aspectMask: Int, description: String, device: VkDevice, stack: MemoryStack): Long {
    val ciImageView = VkImageViewCreateInfo.callocStack(stack)
    ciImageView.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
    ciImageView.image(image)
    ciImageView.viewType(VK_IMAGE_VIEW_TYPE_2D)
    ciImageView.format(format)
    ciImageView.components().set(
        VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY,
        VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY
    )
    ciImageView.subresourceRange().aspectMask(aspectMask)
    ciImageView.subresourceRange().baseMipLevel(0)
    ciImageView.subresourceRange().levelCount(1)
    ciImageView.subresourceRange().baseArrayLayer(0)
    ciImageView.subresourceRange().layerCount(1)

    val pImageView = stack.callocLong(1)
    assertSuccess(
        vkCreateImageView(device, ciImageView, null, pImageView),
        "CreateImageView", description
    )
    return pImageView[0]
}
