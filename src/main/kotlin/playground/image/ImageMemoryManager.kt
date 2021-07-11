package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkMemoryAllocateInfo
import org.lwjgl.vulkan.VkMemoryRequirements
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties
import playground.ApplicationState
import playground.assertSuccess

fun allocateResolutionDependantImageMemory(appState: ApplicationState) {
    stackPush().use {stack ->
        val memoryProps = VkPhysicalDeviceMemoryProperties.callocStack(stack)
        vkGetPhysicalDeviceMemoryProperties(appState.physicalDevice, memoryProps)

        val depthRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetImageMemoryRequirements(appState.device, appState.depthImage!!, depthRequirements)

        val supportedMemoryTypeBits = depthRequirements.memoryTypeBits()

        val memoryTypeIndex = run {
            var supportedMemoryIndex: Int? = null
            for (memoryTypeIndex in 0 until memoryProps.memoryTypeCount()) {
                val supportedByImages = ((1 shl memoryTypeIndex) and supportedMemoryTypeBits) != 0
                val deviceLocal = (memoryProps.memoryTypes(memoryTypeIndex).propertyFlags() and VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0

                // Try to find a memory type index that is both supported and device local
                if (supportedByImages && deviceLocal) {
                    return@run memoryTypeIndex
                }

                // Remember any supported memory type index in case we won't find any that is also device local
                if (supportedByImages) {
                    supportedMemoryIndex = memoryTypeIndex
                }
            }

            supportedMemoryIndex!!
        }

        val memoryType = memoryProps.memoryTypes(memoryTypeIndex)
        println("Selected memory type for resolution-dependant images:")
        println("property bits are ${memoryType.propertyFlags()}")
        val memoryHeap = memoryProps.memoryHeaps(memoryType.heapIndex())
        println("heap size is ${memoryHeap.size()} and heap flags are ${memoryHeap.flags()}")
        println()

        // Currently, this is only used for the depth image memory, but I'm planning to add more in the future
        val offsetDepthMemory = 0L
        val requiredMemorySize = depthRequirements.size()

        val aiMemory = VkMemoryAllocateInfo.callocStack(stack)
        aiMemory.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        aiMemory.memoryTypeIndex(memoryTypeIndex)
        aiMemory.allocationSize(requiredMemorySize)

        val pMemory = stack.callocLong(1)
        assertSuccess(
            vkAllocateMemory(appState.device, aiMemory, null, pMemory),
            "AllocateMemory", "resolution-dependant"
        )
        appState.resolutionDependantMemory = pMemory[0]

        assertSuccess(
            vkBindImageMemory(appState.device, appState.depthImage!!, appState.resolutionDependantMemory!!, offsetDepthMemory),
            "BindImageMemory"
        )
    }
}

fun freeResolutionDependantImageMemory(appState: ApplicationState) {
    if (appState.resolutionDependantMemory != null) {
        vkFreeMemory(appState.device, appState.resolutionDependantMemory!!, null)
    }
}
