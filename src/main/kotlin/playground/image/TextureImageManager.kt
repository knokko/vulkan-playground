package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import playground.ApplicationState
import playground.assertSuccess
import playground.chooseMemoryTypeIndex
import java.awt.Color
import javax.imageio.ImageIO

fun createTextureImageResources(appState: ApplicationState) {
    stackPush().use { stack ->
        val terrainColorImage = ImageIO.read(ApplicationState::class.java.classLoader.getResource("testTerrain.jpg"))

        // For now, the terrain image is the only image
        val totalColorImage = terrainColorImage
        appState.textureColorImage = createImage(
            totalColorImage.width, totalColorImage.height, VK_FORMAT_R8G8B8A8_SRGB, VK_SAMPLE_COUNT_1_BIT,
            VK_IMAGE_USAGE_SAMPLED_BIT or VK_IMAGE_USAGE_TRANSFER_DST_BIT,
            "texture color image", appState.device, stack
        )

        val colorRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetImageMemoryRequirements(appState.device, appState.textureColorImage!!, colorRequirements)

        val memoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            // TODO Also check requirements for the height image
            colorRequirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )!!

        // TODO This needs to be improved after adding height textures
        val totalMemorySize = colorRequirements.size()

        val aiImageMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiImageMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiImageMemory.allocationSize(totalMemorySize)
        aiImageMemory.memoryTypeIndex(memoryTypeIndex)

        val pImageMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiImageMemory, null, pImageMemory),
            "AllocateMemory", "texture images"
        )
        appState.textureImageMemory = pImageMemory[0]

        assertSuccess(
            vkBindImageMemory(appState.device, appState.textureColorImage!!, appState.textureImageMemory!!, 0),
            "BindImageMemory", "texture color image"
        )

        appState.textureColorImageView = createImageView(
            appState.textureColorImage!!, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT,
            "texture color image", appState.device, stack
        )

        // TODO Increase this after adding height texture
        val stagingBufferSize = 4 * totalColorImage.width * totalColorImage.height
        val ciStagingBuffer = VkBufferCreateInfo.callocStack(stack)
        ciStagingBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        ciStagingBuffer.size(stagingBufferSize.toLong())
        ciStagingBuffer.usage(VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
        ciStagingBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pStagingBuffer = stack.callocLong(1)
        assertSuccess(
            vkCreateBuffer(appState.device, ciStagingBuffer, null, pStagingBuffer),
            "CreateBuffer", "texture image staging"
        )
        val stagingBuffer = pStagingBuffer[0]

        val stagingRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetBufferMemoryRequirements(appState.device, stagingBuffer, stagingRequirements)

        val stagingMemoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice, stagingRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
        )!!

        val aiStaging = VkMemoryAllocateInfo.callocStack(stack)
        aiStaging.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiStaging.allocationSize(stagingRequirements.size())
        aiStaging.memoryTypeIndex(stagingMemoryTypeIndex)

        val pStagingMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiStaging, null, pStagingMemory),
            "AllocateMemory", "texture staging"
        )
        val stagingMemory = pStagingMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, stagingBuffer, stagingMemory, 0),
            "BindBufferMemory", "texture staging"
        )

        val ppStaging = stack.callocPointer(1)
        assertSuccess(
            vkMapMemory(appState.device, stagingMemory, 0, VK_WHOLE_SIZE, 0, ppStaging),
            "MapMemory", "texture staging"
        )
        val stagingData = memByteBuffer(ppStaging.get(0), stagingBufferSize)
        for (x in 0 until totalColorImage.width) {
            for (y in 0 until totalColorImage.height) {
                val index = 4 * (x + y * totalColorImage.width)
                val rgba = totalColorImage.getRGB(x, y)
                val color = Color(rgba, true)
                stagingData.put(index, color.red.toByte())
                stagingData.put(index + 1, color.green.toByte())
                stagingData.put(index + 2, color.blue.toByte())
                stagingData.put(index + 3, color.alpha.toByte())
            }
        }
        // TODO Also store the height texture

        val flushRanges = VkMappedMemoryRange.callocStack(1, stack)
        val flushRange = flushRanges[0]
        flushRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        flushRange.memory(stagingMemory)
        flushRange.offset(0)
        flushRange.size(VK_WHOLE_SIZE)

        assertSuccess(
            vkFlushMappedMemoryRanges(appState.device, flushRanges),
            "FlushMappedMemoryRanges", "texture staging"
        )
        vkUnmapMemory(appState.device, stagingMemory)

        val aiCopyBuffer = VkCommandBufferAllocateInfo.callocStack(stack)
        aiCopyBuffer.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
        aiCopyBuffer.commandPool(appState.bufferCopyCommandPool!!)
        aiCopyBuffer.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
        aiCopyBuffer.commandBufferCount(1)

        val pCopyBuffer = stack.callocPointer(1)
        assertSuccess(
            vkAllocateCommandBuffers(appState.device, aiCopyBuffer, pCopyBuffer),
            "AllocateCommandBuffers", "copy staging texture"
        )
        val copyBuffer = VkCommandBuffer(pCopyBuffer.get(0), appState.device)

        val biCopyBuffer = VkCommandBufferBeginInfo.callocStack(stack)
        biCopyBuffer.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
        biCopyBuffer.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

        assertSuccess(
            vkBeginCommandBuffer(copyBuffer, biCopyBuffer),
            "BeginCommandBuffer", "copy staging texture"
        )

        val preCopyBarriers = VkImageMemoryBarrier.callocStack(1, stack)
        val preCopyBarrier = preCopyBarriers[0]
        preCopyBarrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
        preCopyBarrier.srcAccessMask(0)
        preCopyBarrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
        preCopyBarrier.oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        preCopyBarrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
        preCopyBarrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        preCopyBarrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        preCopyBarrier.image(appState.textureColorImage!!)
        preCopyBarrier.subresourceRange { srr ->
            srr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
            srr.baseMipLevel(0)
            srr.levelCount(1)
            srr.baseArrayLayer(0)
            srr.layerCount(1)
        }

        vkCmdPipelineBarrier(
            copyBuffer, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
            0, null, null, preCopyBarriers
        )

        // TODO Also transition height texture

        val colorRegions = VkBufferImageCopy.callocStack(1, stack)
        val colorRegion = colorRegions[0]
        colorRegion.bufferOffset(0)
        colorRegion.bufferRowLength(0)
        colorRegion.bufferImageHeight(0)
        colorRegion.imageSubresource { isr ->
            isr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
            isr.mipLevel(0)
            isr.baseArrayLayer(0)
            isr.layerCount(1)
        }
        colorRegion.imageOffset { offset -> offset.set(0, 0, 0)}
        colorRegion.imageExtent { extent -> extent.set(totalColorImage.width, totalColorImage.height, 1) }

        vkCmdCopyBufferToImage(
            copyBuffer, stagingBuffer, appState.textureColorImage!!, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, colorRegions
        )

        // TODO Also copy to height texture

        val postCopyBarriers = VkImageMemoryBarrier.callocStack(1, stack)
        val postCopyBarrier = postCopyBarriers[0]
        postCopyBarrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
        postCopyBarrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
        postCopyBarrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT)
        postCopyBarrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
        postCopyBarrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
        postCopyBarrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        postCopyBarrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        postCopyBarrier.image(appState.textureColorImage!!)
        postCopyBarrier.subresourceRange { srr ->
            srr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
            srr.baseMipLevel(0)
            srr.levelCount(1)
            srr.baseArrayLayer(0)
            srr.layerCount(1)
        }

        vkCmdPipelineBarrier(
            copyBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0,
            null, null, postCopyBarriers
        )

        // TODO Also transition the height texture to VERTEX shader

        assertSuccess(
            vkEndCommandBuffer(copyBuffer), "EndCommandBuffer", "copy staging texture"
        )

        val siCopies = VkSubmitInfo.callocStack(1, stack)
        val siCopy = siCopies[0]
        siCopy.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        siCopy.waitSemaphoreCount(0)
        siCopy.pCommandBuffers(stack.pointers(copyBuffer.address()))
        siCopy.pSignalSemaphores(null)

        val ciFence = VkFenceCreateInfo.callocStack(stack)
        ciFence.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)

        val pFence = stack.callocLong(1)
        assertSuccess(
            vkCreateFence(appState.device, ciFence, null, pFence),
            "CreateFence", "copy staging textures"
        )

        assertSuccess(
            vkQueueSubmit(appState.graphicsQueue, siCopies, pFence[0]),
            "QueueSubmit", "copy staging textures"
        )

        assertSuccess(
            vkWaitForFences(appState.device, pFence, true, 1_000_000_000),
            "WaitForFences", "copy staging textures"
        )

        vkDestroyFence(appState.device, pFence[0], null)
        vkFreeCommandBuffers(appState.device, appState.bufferCopyCommandPool!!, copyBuffer)
        vkDestroyBuffer(appState.device, stagingBuffer, null)
        vkFreeMemory(appState.device, stagingMemory, null)
    }
}

fun destroyTextureImageResources(appState: ApplicationState) {
    if (appState.textureColorImageView != null) {
        vkDestroyImageView(appState.device, appState.textureColorImageView!!, null)
    }
    if (appState.textureColorImage != null) {
        vkDestroyImage(appState.device, appState.textureColorImage!!, null)
    }
    // TODO Destroy terrain height resources
    if (appState.textureImageMemory != null) {
        vkFreeMemory(appState.device, appState.textureImageMemory!!, null)
    }
}
