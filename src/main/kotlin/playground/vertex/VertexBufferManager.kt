package playground.vertex

import org.joml.Vector3f
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import playground.*

fun createVertexBuffers(appState: ApplicationState) {
    stackPush().use { stack ->

        val ciBuffer = VkBufferCreateInfo.callocStack(stack)
        ciBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        // Size will be filled in later
        // Usage will be filled in later
        ciBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pBuffer = stack.callocLong(1)

        ciBuffer.size(TOTAL_VERTEX_SIZE.toLong())
        ciBuffer.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT or VK_BUFFER_USAGE_TRANSFER_DST_BIT)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "vertex"
        )
        appState.vertexBuffer = pBuffer[0]

        ciBuffer.size(TOTAL_INDEX_SIZE.toLong())
        ciBuffer.usage(VK_BUFFER_USAGE_INDEX_BUFFER_BIT or VK_BUFFER_USAGE_TRANSFER_DST_BIT)
        assertSuccess(
            vkCreateBuffer(appState.device, ciBuffer, null, pBuffer),
            "CreateBuffer", "index"
        )
        appState.indexBuffer = pBuffer[0]

        val memRequirements = VkMemoryRequirements.callocStack(stack)

        vkGetBufferMemoryRequirements(appState.device, appState.vertexBuffer!!, memRequirements)
        val vertexMemoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            memRequirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )!!
        val vertexAllocationSize = memRequirements.size()

        vkGetBufferMemoryRequirements(appState.device, appState.indexBuffer!!, memRequirements)
        val indexMemoryTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            memRequirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )!!
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

        val ciStagingBuffer = VkBufferCreateInfo.callocStack(stack)
        ciStagingBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        ciStagingBuffer.size((TOTAL_VERTEX_SIZE + TOTAL_INDEX_SIZE).toLong())
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
        val memTypeIndex = chooseMemoryTypeIndex(
            appState.physicalDevice,
            memRequirements.memoryTypeBits(),
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
        )!!

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

        val stagingData = memByteBuffer(ppStagingData[0], TOTAL_VERTEX_SIZE + TOTAL_INDEX_SIZE)

        // Fill the staging data...
        var vertexOffset = 0
        var indexOffset = 0

        for (request in MODEL_REQUESTS) {
            val requestVertices = BasicVertex.createArray(stagingData, vertexOffset * BasicVertex.SIZE, request.numVertices.toLong())
            vertexOffset += request.numVertices

            stagingData.position(TOTAL_VERTEX_SIZE + indexOffset * INDEX_SIZE)
            indexOffset += request.numIndices
            stagingData.limit(TOTAL_VERTEX_SIZE + indexOffset * INDEX_SIZE)

            val requestIndices = stagingData.asIntBuffer()

            request.fill(requestVertices, requestIndices)
        }

        // Always reset this!
        stagingData.position(0)
        stagingData.limit(stagingData.capacity())

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
        vertexCopyRegions[0].size(TOTAL_VERTEX_SIZE.toLong())

        vkCmdCopyBuffer(copyCommand, stagingBuffer, appState.vertexBuffer!!, vertexCopyRegions)

        val indexCopyRegions = VkBufferCopy.callocStack(1, stack)
        indexCopyRegions[0].srcOffset(TOTAL_VERTEX_SIZE.toLong())
        indexCopyRegions[0].dstOffset(0)
        indexCopyRegions[0].size(TOTAL_INDEX_SIZE.toLong())

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

        // And call the store methods of the model requests
        vertexOffset = 0
        indexOffset = 0
        for (request in MODEL_REQUESTS) {
            val resultModel = Model(
                vertexOffset,
                request.numVertices,
                indexOffset,
                request.numIndices,
                request.numMatrices
            )
            request.store(appState, resultModel)

            vertexOffset += request.numVertices
            indexOffset += request.numIndices
        }
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

val MODEL_REQUESTS = arrayOf(
    requestTerrainModel(2) { appState, resultModel -> appState.terrainModels.model5 = resultModel },
    requestTerrainModel(2) { appState, resultModel -> appState.terrainModels.model15 = resultModel },
    requestTerrainModel(2) { appState, resultModel -> appState.terrainModels.model50 = resultModel }
)

val TOTAL_NUM_VERTICES = MODEL_REQUESTS.sumOf { request -> request.numVertices }

val TOTAL_NUM_INDICES = MODEL_REQUESTS.sumOf { request -> request.numIndices }

// For now, I will only use 4-byte indices
const val INDEX_SIZE = 4
const val INDEX_TYPE = VK_INDEX_TYPE_UINT32

val TOTAL_VERTEX_SIZE = TOTAL_NUM_VERTICES * BasicVertex.SIZE
val TOTAL_INDEX_SIZE = TOTAL_NUM_INDICES * INDEX_SIZE