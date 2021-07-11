package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkMemoryAllocateInfo
import org.lwjgl.vulkan.VkMemoryRequirements
import playground.ApplicationState
import playground.assertSuccess
import playground.chooseMemoryTypeIndex

fun allocateResolutionDependantImageMemory(appState: ApplicationState) {
    stackPush().use {stack ->

        val depthRequirements = VkMemoryRequirements.callocStack(stack)
        vkGetImageMemoryRequirements(appState.device, appState.depthImage!!, depthRequirements)

        val sufficientMemoryTypeIndex = chooseMemoryTypeIndex(appState.physicalDevice, depthRequirements, 0)!!
        val niceMemoryTypeIndex =
            chooseMemoryTypeIndex(appState.physicalDevice, depthRequirements, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

        val memoryTypeIndex = niceMemoryTypeIndex?: sufficientMemoryTypeIndex

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
            vkBindImageMemory(
                appState.device,
                appState.depthImage!!,
                appState.resolutionDependantMemory!!,
                offsetDepthMemory
            ),
            "BindImageMemory"
        )
    }
}

fun freeResolutionDependantImageMemory(appState: ApplicationState) {
    if (appState.resolutionDependantMemory != null) {
        vkFreeMemory(appState.device, appState.resolutionDependantMemory!!, null)
    }
}
