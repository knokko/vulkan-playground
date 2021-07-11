package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*

// I will probably need to increase this number later
const val MAX_INDIRECT_DRAW_COUNT = 20

val INDIRECT_DRAW_STRIDE = VkDrawIndexedIndirectCommand.SIZEOF

const val MAX_NUM_TRANSFORMATION_MATRICES = 200

// TODO Enable and check drawIndirectFirstInstance

fun createIndirectDrawBuffer(appState: ApplicationState) {
    stackPush().use { stack ->

        val ciIndirectBuffer = VkBufferCreateInfo.callocStack(stack)
        ciIndirectBuffer.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        // 4 bytes for Count and the rest of the bytes is for the draw commands
        ciIndirectBuffer.size(4L + MAX_INDIRECT_DRAW_COUNT * INDIRECT_DRAW_STRIDE)
        ciIndirectBuffer.usage(VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT)
        ciIndirectBuffer.sharingMode(VK_SHARING_MODE_EXCLUSIVE)

        val pIndirectBuffer = stack.callocLong(1)
        assertSuccess(
            vkCreateBuffer(appState.device, ciIndirectBuffer, null, pIndirectBuffer),
            "CreateBuffer", "indirect"
        )
        appState.indirectDrawBuffer = pIndirectBuffer[0]

        val memRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetBufferMemoryRequirements(appState.device, appState.indirectDrawBuffer!!, memRequirements)

        val memoryTypeIndex = chooseMemoryTypeIndex(appState.physicalDevice, memRequirements, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)!!

        val aiIndirectMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiIndirectMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiIndirectMemory.allocationSize(memRequirements.size())
        aiIndirectMemory.memoryTypeIndex(memoryTypeIndex)

        val pIndirectMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiIndirectMemory, null, pIndirectMemory),
            "AllocateMemory", "indirect"
        )
        appState.indirectMemory = pIndirectMemory[0]

        assertSuccess(
            vkBindBufferMemory(appState.device, appState.indirectDrawBuffer!!, appState.indirectMemory!!, 0),
            "BindBufferMemory", "indirect"
        )

        // I might make this more complicated in the future, but this should do for now
        appState.indirectCountOffset = 0
        appState.indirectDrawOffset = 4
    }
}

fun destroyIndirectDrawBuffer(appState: ApplicationState) {
    if (appState.indirectDrawBuffer != null) {
        vkDestroyBuffer(appState.device, appState.indirectDrawBuffer!!, null)
    }
    if (appState.indirectMemory != null) {
        vkFreeMemory(appState.device, appState.indirectMemory!!, null)
    }
}
