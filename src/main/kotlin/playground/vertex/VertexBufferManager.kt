package playground.vertex

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkBufferCreateInfo
import org.lwjgl.vulkan.VkMemoryAllocateInfo
import org.lwjgl.vulkan.VkMemoryRequirements
import playground.ApplicationState
import playground.assertSuccess
import playground.chooseMemoryTypeIndex

fun createVertexBuffers(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciBuffer = VkBufferCreateInfo.callocStack(stack)
        ciBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        // Size will be filled in later
        // Usage will be filled in later
        ciBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pBuffer = stack.callocLong(1)

        ciBuffer.size(TOTAL_NUM_VERTICES * BasicVertex.SIZE)
        ciBuffer.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "vertex"
        )
        appState.vertexBuffer = pBuffer[0]

        ciBuffer.size(TOTAL_NUM_INDICES * INDEX_SIZE)
        ciBuffer.usage(VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "index"
        )
        appState.indexBuffer = pBuffer[0]

        val memRequirements = VkMemoryRequirements.callocStack(stack)

        vkGetBufferMemoryRequirements(appState.device, appState.vertexBuffer!!, memRequirements)
        val vertexMemoryTypeIndex = chooseMemoryTypeIndex(appState.physicalDevice, memRequirements, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)!!
        val vertexAllocationSize = memRequirements.size()

        vkGetBufferMemoryRequirements(appState.device, appState.indexBuffer!!, memRequirements)
        val indexMemoryTypeIndex = chooseMemoryTypeIndex(appState.physicalDevice, memRequirements, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)!!
        val indexAllocationSize = memRequirements.size()

        val aiMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)

        val pMemory = stack.callocLong(1)

        aiMemory.allocationSize(vertexAllocationSize)
        aiMemory.memoryTypeIndex(vertexMemoryTypeIndex)
        assertSuccess(
            vkAllocateMemory(appState.device, aiMemory, null, pMemory),
            "AllocateMemory", "vertex"
        )
        appState.vertexMemory = pMemory[0]

        aiMemory.allocationSize(indexAllocationSize)
        aiMemory.memoryTypeIndex(indexMemoryTypeIndex)
        assertSuccess(
            vkAllocateMemory(appState.device, aiMemory, null, pMemory),
            "AllocateMemory", "index"
        )
        appState.indexMemory = pMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, appState.vertexBuffer!!, appState.vertexMemory!!, 0),
            "BindBufferMemory", "vertex"
        )
        assertSuccess(
            vkBindBufferMemory(appState.device, appState.indexBuffer!!, appState.indexMemory!!, 0),
            "BindBufferMemory", "index"
        )
    }
}

fun destroyVertexBuffers(appState: ApplicationState) {
    if (appState.vertexBuffer != null) {
        vkDestroyBuffer(appState.device, appState.vertexBuffer!!, null)
    }
    if (appState.vertexMemory != null) {
        vkFreeMemory(appState.device, appState.vertexMemory!!, null)
    }
    if (appState.indexBuffer != null) {
        vkDestroyBuffer(appState.device, appState.indexBuffer!!, null)
    }
    if (appState.indexMemory != null) {
        vkFreeMemory(appState.device, appState.indexMemory!!, null)
    }
}

val NUM_MODEL_VERTICES = arrayOf(4L)
val TOTAL_NUM_VERTICES = NUM_MODEL_VERTICES.sum()

val NUM_MODEL_INDICES = arrayOf(6L)
val TOTAL_NUM_INDICES = NUM_MODEL_INDICES.sum()

// For now, I will only use 4-byte indices
const val INDEX_SIZE = 4
const val INDEX_TYPE = VK_INDEX_TYPE_UINT32