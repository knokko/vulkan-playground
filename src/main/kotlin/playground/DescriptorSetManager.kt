package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*

fun createDescriptorSets(appState: ApplicationState) {
    createUniformBuffer(appState)
    createStorageBuffer(appState)
    createBasicDescriptorSet(appState)
}

fun createUniformBuffer(appState: ApplicationState) {
    stackPush().use { stack ->

        // Currently just 1 matrix of 4 x 4 floats consisting of 4 bytes each
        val cameraMatrixSize = 4 * 4 * 4
        val cameraPositionSize = 3 * 4
        val uniformSize = cameraMatrixSize + cameraPositionSize

        val ciBuffer = VkBufferCreateInfo.callocStack(stack)
        ciBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        ciBuffer.size(uniformSize.toLong())
        ciBuffer.usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
        ciBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pBuffer = stack.callocLong(1)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "uniform"
        )
        appState.uniformBuffer = pBuffer[0]

        val requirements = VkMemoryRequirements.callocStack(stack)
        vkGetBufferMemoryRequirements(appState.device, appState.uniformBuffer!!, requirements)

        val memoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            requirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
        )!!

        val aiMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiMemory.allocationSize(requirements.size())
        aiMemory.memoryTypeIndex(memoryTypeIndex)

        val pMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiMemory, null, pMemory),
            "AllocateMemory", "uniform"
        )
        appState.uniformMemory = pMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, appState.uniformBuffer!!, appState.uniformMemory!!, 0),
            "BindBufferMemory", "uniform"
        )

        val ppUniformData = stack.callocPointer(1)
        assertSuccess(
            vkMapMemory(appState.device, appState.uniformMemory!!, 0, aiMemory.allocationSize(), 0, ppUniformData),
            "MapMemory", "uniform"
        )
        appState.uniformData = memByteBuffer(ppUniformData[0], uniformSize)
    }
}

fun createStorageBuffer(appState: ApplicationState) {
    stackPush().use { stack ->

        // Each transformation matrix consists of 4 x 4 floats of 4 bytes each
        // Currently, nothing else is stored in the storage buffer
        val storageSize = MAX_NUM_TRANSFORMATION_MATRICES * 4 * 4 * 4

        val ciBuffer = VkBufferCreateInfo.callocStack(stack)
        ciBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        ciBuffer.size(storageSize.toLong())
        ciBuffer.usage(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT)
        ciBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pBuffer = stack.callocLong(1)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "storage"
        )
        appState.storageBuffer = pBuffer[0]

        val requirements = VkMemoryRequirements.callocStack(stack)
        vkGetBufferMemoryRequirements(appState.device, appState.storageBuffer!!, requirements)

        val memoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            requirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
        )!!

        val aiMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiMemory.allocationSize(requirements.size())
        aiMemory.memoryTypeIndex(memoryTypeIndex)

        val pMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiMemory, null, pMemory),
            "AllocateMemory", "storage"
        )
        appState.storageMemory = pMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, appState.storageBuffer!!, appState.storageMemory!!, 0),
            "BindBufferMemory", "storage"
        )

        val ppStorageData = stack.callocPointer(1)
        assertSuccess(
            vkMapMemory(appState.device, appState.storageMemory!!, 0, aiMemory.allocationSize(), 0, ppStorageData),
            "MapMemory", "storage"
        )
        appState.storageData = memByteBuffer(ppStorageData[0], storageSize)
    }
}

fun createBasicDescriptorSet(appState: ApplicationState) {
    stackPush().use { stack ->

        // Uniform descriptor, texture sampling descriptor, and storage descriptor
        val poolSizes = VkDescriptorPoolSize.callocStack(3, stack)
        val uniformPoolSize = poolSizes[0]
        uniformPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        uniformPoolSize.descriptorCount(1)
        val samplingPoolSize = poolSizes[1]
        samplingPoolSize.type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        samplingPoolSize.descriptorCount(2)
        val storagePoolSize = poolSizes[2]
        storagePoolSize.type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
        storagePoolSize.descriptorCount(1)

        val ciDescriptorPool = VkDescriptorPoolCreateInfo.callocStack(stack)
        ciDescriptorPool.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
        // I will only need 1 descriptor set
        ciDescriptorPool.maxSets(1)
        ciDescriptorPool.pPoolSizes(poolSizes)

        val pDescriptorPool = stack.callocLong(1)
        assertSuccess(
            vkCreateDescriptorPool(appState.device, ciDescriptorPool, null, pDescriptorPool),
            "CreateDescriptorPool"
        )
        appState.basicDescriptorPool = pDescriptorPool[0]

        val aiDescriptor = VkDescriptorSetAllocateInfo.callocStack(stack)
        aiDescriptor.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
        aiDescriptor.descriptorPool(appState.basicDescriptorPool!!)
        aiDescriptor.pSetLayouts(stack.longs(appState.basicDescriptorSetLayout!!))

        val pDescriptorSet = stack.callocLong(1)
        assertSuccess(
            vkAllocateDescriptorSets(appState.device, aiDescriptor, pDescriptorSet),
            "AllocateDescriptorSets"
        )
        appState.basicDescriptorSet = pDescriptorSet[0]

        val biUniforms = VkDescriptorBufferInfo.callocStack(1, stack)
        val biUniform = biUniforms[0]
        biUniform.buffer(appState.uniformBuffer!!)
        biUniform.offset(0)
        biUniform.range(VK_WHOLE_SIZE)

        val iiColorSamplers = VkDescriptorImageInfo.callocStack(1, stack)
        val iiColorSampler = iiColorSamplers[0]
        iiColorSampler.sampler(appState.basicImageSampler!!)
        iiColorSampler.imageView(appState.textureColorImageView!!)
        iiColorSampler.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)

        val iiHeightSamplers = VkDescriptorImageInfo.callocStack(1, stack)
        val iiHeightSampler = iiHeightSamplers[0]
        iiHeightSampler.sampler(appState.basicImageSampler!!)
        iiHeightSampler.imageView(appState.textureHeightImageView!!)
        iiHeightSampler.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)

        val biStorages = VkDescriptorBufferInfo.callocStack(1, stack)
        val biStorage = biStorages[0]
        biStorage.buffer(appState.storageBuffer!!)
        biStorage.offset(0)
        biStorage.range(VK_WHOLE_SIZE)

        val descriptorWrites = VkWriteDescriptorSet.callocStack(4, stack)
        val uniformWrite = descriptorWrites[0]
        uniformWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
        uniformWrite.dstSet(appState.basicDescriptorSet!!)
        uniformWrite.dstBinding(0)
        uniformWrite.dstArrayElement(0)
        uniformWrite.descriptorCount(1)
        uniformWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        uniformWrite.pBufferInfo(biUniforms)

        val colorSamplerWrite = descriptorWrites[1]
        colorSamplerWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
        colorSamplerWrite.dstSet(appState.basicDescriptorSet!!)
        colorSamplerWrite.dstBinding(1)
        colorSamplerWrite.dstArrayElement(0)
        colorSamplerWrite.descriptorCount(1)
        colorSamplerWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        colorSamplerWrite.pImageInfo(iiColorSamplers)

        val heightSamplerWrite = descriptorWrites[2]
        heightSamplerWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
        heightSamplerWrite.dstSet(appState.basicDescriptorSet!!)
        heightSamplerWrite.dstBinding(2)
        heightSamplerWrite.dstArrayElement(0)
        heightSamplerWrite.descriptorCount(1)
        heightSamplerWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        heightSamplerWrite.pImageInfo(iiHeightSamplers)

        val storageWrite = descriptorWrites[3]
        storageWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
        storageWrite.dstSet(appState.basicDescriptorSet!!)
        storageWrite.dstBinding(3)
        storageWrite.dstArrayElement(0)
        storageWrite.descriptorCount(1)
        storageWrite.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
        storageWrite.pBufferInfo(biStorages)

        vkUpdateDescriptorSets(appState.device, descriptorWrites, null)
    }
}

fun destroyDescriptorSets(appState: ApplicationState) {
    // There is no need to free the basic descriptor set explicitly (it is not even allowed unless we set a flag)
    if (appState.basicDescriptorPool != null) {
        vkDestroyDescriptorPool(appState.device, appState.basicDescriptorPool!!, null)
    }

    if (appState.uniformBuffer != null) {
        vkDestroyBuffer(appState.device, appState.uniformBuffer!!, null)
    }
    if (appState.uniformMemory != null) {
        vkUnmapMemory(appState.device, appState.uniformMemory!!)
        vkFreeMemory(appState.device, appState.uniformMemory!!, null)
    }
    if (appState.storageBuffer != null) {
        vkDestroyBuffer(appState.device, appState.storageBuffer!!, null)
    }
    if (appState.storageMemory != null) {
        vkUnmapMemory(appState.device, appState.storageMemory!!)
        vkFreeMemory(appState.device, appState.storageMemory!!, null)
    }
}
