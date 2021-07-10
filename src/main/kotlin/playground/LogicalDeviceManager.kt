package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkDeviceCreateInfo
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo
import org.lwjgl.vulkan.VkQueueFamilyProperties

fun initLogicalDevice(appState: ApplicationState) {
    stackPush().use { stack ->

        val pNumQueueFamilies = stack.callocInt(1)
        vkGetPhysicalDeviceQueueFamilyProperties(appState.physicalDevice, pNumQueueFamilies, null)
        val numQueueFamilies = pNumQueueFamilies[0]

        val pQueueFamilies = VkQueueFamilyProperties.callocStack(numQueueFamilies, stack)
        vkGetPhysicalDeviceQueueFamilyProperties(appState.physicalDevice, pNumQueueFamilies, pQueueFamilies)

        val firstGraphicsFamilyIndex = run {
            for (familyIndex in 0 until numQueueFamilies) {
                if (pQueueFamilies[familyIndex].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
                    return@run familyIndex
                }
            }

            throw Error("The chosen physical device must have a graphics queue family")
        }

        val pCiQueues = VkDeviceQueueCreateInfo.callocStack(1, stack)
        val ciQueue = pCiQueues[0]
        ciQueue.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
        ciQueue.queueFamilyIndex(firstGraphicsFamilyIndex)
        // We will only use 1 queue (we are planning to minimise the number of queue submits anyway)
        ciQueue.pQueuePriorities(stack.floats(0.5f))

        val pExtensions = stack.callocPointer(appState.deviceExtensions.size)
        for ((index, extension) in appState.deviceExtensions.withIndex()) {
            pExtensions.put(index, stack.UTF8(extension))
        }

        val ciDevice = VkDeviceCreateInfo.callocStack(stack)
        ciDevice.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
        ciDevice.pQueueCreateInfos(pCiQueues)
        ciDevice.ppEnabledExtensionNames(pExtensions)
        // TODO I'm not sure whether I need to enable the draw indirect count feature, so I will find out soon

        val pDevice = stack.callocPointer(1)
        assertSuccess(vkCreateDevice(appState.physicalDevice, ciDevice, null, pDevice), "CreateDevice")
        appState.device = VkDevice(pDevice.get(0), appState.physicalDevice, ciDevice)
        appState.queueFamilyIndex = firstGraphicsFamilyIndex
    }
}

fun destroyLogicalDevice(appState: ApplicationState) {
    appState.destroyDevice()
}
