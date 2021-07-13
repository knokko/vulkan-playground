package playground.vertex

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
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
        ciBuffer.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT or VK_BUFFER_USAGE_TRANSFER_DST_BIT)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "vertex"
        )
        appState.vertexBuffer = pBuffer[0]

        ciBuffer.size(TOTAL_NUM_INDICES * INDEX_SIZE)
        ciBuffer.usage(VK_BUFFER_USAGE_INDEX_BUFFER_BIT or VK_BUFFER_USAGE_TRANSFER_DST_BIT)
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

fun fillVertexBuffers(appState: ApplicationState) {
    stackPush().use { stack ->

        val totalVertexSize = TOTAL_NUM_VERTICES * BasicVertex.SIZE
        val totalIndexSize = TOTAL_NUM_INDICES * INDEX_SIZE

        val ciStagingBuffer = VkBufferCreateInfo.callocStack(stack)
        ciStagingBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        ciStagingBuffer.size(totalVertexSize + totalIndexSize)
        ciStagingBuffer.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
        ciStagingBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pStagingBuffer = stack.callocLong(1)
        assertSuccess(
            vkCreateBuffer(appState.device, ciStagingBuffer, null, pStagingBuffer),
            "CreateBuffer", "staging"
        )
        val stagingBuffer = pStagingBuffer[0]

        val memRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetBufferMemoryRequirements(appState.device, stagingBuffer, memRequirements)

        val stagingMemorySize = memRequirements.size()
        val memTypeIndex = chooseMemoryTypeIndex(appState.physicalDevice, memRequirements, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)!!

        val aiStagingMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiStagingMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiStagingMemory.allocationSize(stagingMemorySize)
        aiStagingMemory.memoryTypeIndex(memTypeIndex)

        val pStagingMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiStagingMemory, null, pStagingMemory),
            "AllocateMemory", "staging"
        )
        val stagingMemory = pStagingMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, stagingBuffer, stagingMemory, 0),
            "BindBufferMemory", "staging"
        )

        val ppStagingData = stack.callocPointer(1)
        assertSuccess(
            vkMapMemory(appState.device, stagingMemory, 0, stagingMemorySize, 0, ppStagingData),
            "MapMemory", "staging"
        )

        val stagingData = memByteBuffer(ppStagingData[0], (totalVertexSize + totalIndexSize).toInt())
        val vertices = BasicVertex.createArray(stagingData, 0, TOTAL_NUM_VERTICES)

        stagingData.position(totalVertexSize.toInt())
        val indices = stagingData.asIntBuffer()
        stagingData.position(0)

        // This should become more complex when there are more models
        vertices[0].position.x = -1f
        vertices[0].position.y = -1f
        vertices[0].position.z = 0.5f
        // Normals will be set later
        vertices[0].textureCoordinates.x = 0f
        vertices[0].textureCoordinates.y = 0f
        vertices[0].matrixIndex = 0

        vertices[1].position.x = 1f
        vertices[1].position.y = -1f
        vertices[1].position.z = 0.5f
        vertices[1].textureCoordinates.x = 1f
        vertices[1].textureCoordinates.y = 0f
        vertices[1].matrixIndex = 0

        vertices[2].position.x = 1f
        vertices[2].position.y = 1f
        vertices[2].position.z = 0.5f
        vertices[2].textureCoordinates.x = 1f
        vertices[2].textureCoordinates.y = 1f
        vertices[2].matrixIndex = 0

        vertices[3].position.x = -1f
        vertices[3].position.y = 1f
        vertices[3].position.z = 0.5f
        vertices[3].textureCoordinates.x = 0f
        vertices[3].textureCoordinates.y = 1f
        vertices[3].matrixIndex = 0

        for (vertex in vertices) {
            vertex.normal.x = 0f
            vertex.normal.y = 0f
            vertex.normal.z = -1f
        }

        indices.put(0, 0)
        indices.put(1, 1)
        indices.put(2, 2)
        indices.put(3, 2)
        indices.put(4, 3)
        indices.put(5, 0)

        val flushRange = VkMappedMemoryRange.callocStack(stack)
        flushRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
        flushRange.memory(stagingMemory)
        flushRange.offset(0)
        flushRange.size(stagingMemorySize)

        assertSuccess(
            vkFlushMappedMemoryRanges(appState.device, flushRange),
            "FlushMappedMemoryRanges", "staging"
        )

        vkUnmapMemory(appState.device, stagingMemory)

        val aiCopyCommand = VkCommandBufferAllocateInfo.callocStack(stack)
        aiCopyCommand.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
        aiCopyCommand.commandPool(appState.bufferCopyCommandPool!!)
        aiCopyCommand.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
        aiCopyCommand.commandBufferCount(1)

        val pCopyCommand = stack.callocPointer(1)
        assertSuccess(
            vkAllocateCommandBuffers(appState.device, aiCopyCommand, pCopyCommand),
            "AllocateCommandBuffers", "copy"
        )
        val copyCommand = VkCommandBuffer(pCopyCommand[0], appState.device)

        val biCopy = VkCommandBufferBeginInfo.callocStack(stack)
        biCopy.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
        biCopy.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

        assertSuccess(
            vkBeginCommandBuffer(copyCommand, biCopy),
            "BeginCommandBuffer", "copy"
        )

        val vertexCopyRegions = VkBufferCopy.callocStack(1, stack)
        vertexCopyRegions[0].srcOffset(0)
        vertexCopyRegions[0].dstOffset(0)
        vertexCopyRegions[0].size(totalVertexSize)

        vkCmdCopyBuffer(copyCommand, stagingBuffer, appState.vertexBuffer!!, vertexCopyRegions)

        val indexCopyRegions = VkBufferCopy.callocStack(1, stack)
        indexCopyRegions[0].srcOffset(totalVertexSize)
        indexCopyRegions[0].dstOffset(0)
        indexCopyRegions[0].size(totalIndexSize)

        vkCmdCopyBuffer(copyCommand, stagingBuffer, appState.indexBuffer!!, indexCopyRegions)

        assertSuccess(
            vkEndCommandBuffer(copyCommand),
            "EndCommandBuffer", "copy"
        )

        val siCopy = VkSubmitInfo.callocStack(1, stack)
        siCopy.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        siCopy.waitSemaphoreCount(0)
        siCopy.pSignalSemaphores(null)
        siCopy.pCommandBuffers(stack.pointers(copyCommand.address()))

        val ciCopyFence = VkFenceCreateInfo.callocStack(stack)
        ciCopyFence.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)

        val pCopyFence = stack.callocLong(1)
        assertSuccess(
            vkCreateFence(appState.device, ciCopyFence, null, pCopyFence),
            "CreateFence", "copy"
        )
        val copyFence = pCopyFence[0]

        assertSuccess(
            vkQueueSubmit(appState.graphicsQueue, siCopy, copyFence),
            "QueueSubmit", "copy"
        )

        assertSuccess(
            // The timeout shouldn't be reached
            vkWaitForFences(appState.device, pCopyFence, true, 1_000_000_000),
            "WaitForFences", "copy command"
        )

        vkFreeCommandBuffers(appState.device, appState.bufferCopyCommandPool!!, copyCommand)
        vkDestroyFence(appState.device, copyFence, null)
        vkDestroyBuffer(appState.device, stagingBuffer, null)
        vkFreeMemory(appState.device, stagingMemory, null)
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
