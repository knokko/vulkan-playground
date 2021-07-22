package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*

fun initLogicalDevice(appState: ApplicationState) {
    stackPush().use { stack ->

        val pNumQueueFamilies = stack.callocInt(1)
        vkGetPhysicalDeviceQueueFamilyProperties(appState.physicalDevice, pNumQueueFamilies, null)
        val numQueueFamilies = pNumQueueFamilies[0]

        val pQueueFamilies = VkQueueFamilyProperties.callocStack(numQueueFamilies, stack)
        vkGetPhysicalDeviceQueueFamilyProperties(appState.physicalDevice, pNumQueueFamilies, pQueueFamilies)

        val firstGraphicsFamilyIndex = run {
            for (familyIndex in 0 until numQueueFamilies) {
                if (isGraphicsQueueFamilySuitable(appState, stack, appState.physicalDevice, familyIndex, pQueueFamilies[familyIndex])) {
                    return@run familyIndex
                }
            }

            throw Error("The chosen physical device must have a suitable graphics queue family")
        }

        println("Picking queue family index $firstGraphicsFamilyIndex")
        println()

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

        println("Enabling ${appState.deviceExtensions.size} device extensions:")
        for (extension in appState.deviceExtensions) {
            println(extension)
        }
        println()

        val deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack)
        deviceFeatures.drawIndirectFirstInstance(true)
        deviceFeatures.samplerAnisotropy(true)
        deviceFeatures.tessellationShader(true)

        val ciDevice = VkDeviceCreateInfo.callocStack(stack)
        ciDevice.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
        ciDevice.pQueueCreateInfos(pCiQueues)
        ciDevice.ppEnabledExtensionNames(pExtensions)
        ciDevice.pEnabledFeatures(deviceFeatures)

        val pDevice = stack.callocPointer(1)
        assertSuccess(vkCreateDevice(appState.physicalDevice, ciDevice, null, pDevice), "CreateDevice")
        appState.device = VkDevice(pDevice.get(0), appState.physicalDevice, ciDevice)
        appState.queueFamilyIndex = firstGraphicsFamilyIndex
    }
}

fun destroyLogicalDevice(appState: ApplicationState) {
    appState.destroyDevice()
}
