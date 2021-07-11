package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo

fun createDescriptorSets(appState: ApplicationState) {
    createBasicDescriptorSet(appState)
}

fun createBasicDescriptorSet(appState: ApplicationState) {
    stackPush().use { stack ->

        // Just 1 uniform descriptor and 1 storage descriptor
        val poolSizes = VkDescriptorPoolSize.callocStack(2, stack)
        val uniformPoolSize = poolSizes[0]
        uniformPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        uniformPoolSize.descriptorCount(1)
        val storagePoolSize = poolSizes[1]
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
    }
}

fun destroyDescriptorSets(appState: ApplicationState) {
    // There is no need to free the basic descriptor set explicitly (it is not even allowed unless we set a flag)
    if (appState.basicDescriptorPool != null) {
        vkDestroyDescriptorPool(appState.device, appState.basicDescriptorPool!!, null)
    }
}
