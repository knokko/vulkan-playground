package playground

import org.lwjgl.glfw.GLFWVulkan.glfwGetPhysicalDevicePresentationSupport
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRDrawIndirectCount.VK_KHR_DRAW_INDIRECT_COUNT_EXTENSION_NAME
import org.lwjgl.vulkan.KHRShaderDrawParameters.VK_KHR_SHADER_DRAW_PARAMETERS_EXTENSION_NAME
import org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR
import org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
import org.lwjgl.vulkan.VK10.*
import java.nio.ByteBuffer

val requiredDeviceExtensions = arrayOf(
    VK_KHR_SWAPCHAIN_EXTENSION_NAME,
    VK_KHR_DRAW_INDIRECT_COUNT_EXTENSION_NAME,
    VK_KHR_SHADER_DRAW_PARAMETERS_EXTENSION_NAME
)

private class DeviceScore {
    lateinit var name: String
    // Initially true, but will be replaced with false if any extension is missing
    var hasRequiredExtensions = true
    // Initially false, but will be replaced with true if all required features are present
    var hasRequiredFeatures = false
    // Initially false, but will be set to true if a graphics queue is found
    var hasSuitableGraphicsQueue = false
    // 2 for discrete, 1 for integrated, 0 for everything else
    var typeScore = 0
    // The sum of the size of the memory heaps
    var memorySize = 0L
}

fun choosePhysicalDevice(appState: ApplicationState) {
    stackPush().use {stack ->
        val pNumDevices = stack.callocInt(1)
        assertSuccess(
            vkEnumeratePhysicalDevices(appState.instance, pNumDevices, null),
            "EnumeratePhysicalDevices", "count"
        )
        val numDevices = pNumDevices[0]

        if (numDevices == 0) {
            throw UnsupportedOperationException("No physical device (graphics card) with Vulkan support was found")
        }

        val pDevices = stack.callocPointer(numDevices)
        assertSuccess(
            vkEnumeratePhysicalDevices(appState.instance, pNumDevices, pDevices),
            "EnumeratePhysicalDevices", "devices"
        )


        println("There are $numDevices physical devices with Vulkan support:")
        val scores = Array(numDevices) { DeviceScore() }
        for (index in 0 until numDevices) {

            val device = VkPhysicalDevice(pDevices[index], appState.instance)
            val props = VkPhysicalDeviceProperties.callocStack(stack)
            vkGetPhysicalDeviceProperties(device, props)
            scores[index].name = props.deviceNameString()

            val pNumExtensions = stack.callocInt(1)
            assertSuccess(
                vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, pNumExtensions, null),
                "EnumerateDeviceExtensionProperties", "count $index"
            )
            val numExtensions = pNumExtensions[0]

            val pExtensions = VkExtensionProperties.callocStack(numExtensions, stack)
            assertSuccess(
                vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, pNumExtensions, pExtensions),
                "EnumerateDeviceExtensionProperties", "extensions $index"
            )

            val extensions = HashSet<String>(numExtensions)
            for (extensionIndex in 0 until numExtensions) {
                extensions.add(pExtensions[extensionIndex].extensionNameString())
            }

            println("Device $index (${props.deviceNameString()}) has $numExtensions extensions:")
            for (extension in extensions) {
                println(extension)
            }
            println()

            for (extension in requiredDeviceExtensions) {
                if (!extensions.contains(extension)) {
                    println("Device $index is not sufficient because it misses extension $extension")
                    scores[index].hasRequiredExtensions = false
                    break
                }
            }

            val deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack)
            vkGetPhysicalDeviceFeatures(device, deviceFeatures)
            scores[index].hasRequiredFeatures =
                deviceFeatures.drawIndirectFirstInstance() && deviceFeatures.samplerAnisotropy() &&
                        deviceFeatures.tessellationShader()

            val pNumQueueFamilies = stack.callocInt(1)
            vkGetPhysicalDeviceQueueFamilyProperties(device, pNumQueueFamilies, null)
            val numQueueFamilies = pNumQueueFamilies[0]

            val pQueueFamilyProps = VkQueueFamilyProperties.callocStack(numQueueFamilies, stack)
            vkGetPhysicalDeviceQueueFamilyProperties(device, pNumQueueFamilies, pQueueFamilyProps)
            for (familyIndex in 0 until numQueueFamilies) {
                val queueFamilyProps = pQueueFamilyProps[familyIndex]
                if (isGraphicsQueueFamilySuitable(appState, stack, device, familyIndex, queueFamilyProps)) {
                    scores[index].hasSuitableGraphicsQueue = true
                    break
                }
            }

            if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                scores[index].typeScore = 2
            } else if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                scores[index].typeScore = 1
            }

            val memoryProps = VkPhysicalDeviceMemoryProperties.callocStack(stack)
            vkGetPhysicalDeviceMemoryProperties(device, memoryProps)

            for (heapIndex in 0 until memoryProps.memoryHeapCount()) {
                scores[index].memorySize += memoryProps.memoryHeaps(heapIndex).size()
            }

            // TODO This gives quite interesting results. I should look for optimizations here later
            println("There are ${memoryProps.memoryTypeCount()} memory types:")
            for (memIndex in 0 until memoryProps.memoryTypeCount()) {
                println("Type $index: flags are ${memoryProps.memoryTypes(memIndex).propertyFlags()} at heap ${memoryProps.memoryTypes(memIndex).heapIndex()}")
            }
            println()

            println("There are ${memoryProps.memoryHeapCount()} memory heaps:")
            for (memIndex in 0 until memoryProps.memoryHeapCount()) {
                println("Heap $index: flags are ${memoryProps.memoryHeaps(memIndex).flags()} and size is ${memoryProps.memoryHeaps(memIndex).size()}")
            }
            println()
        }
        println()

        val chosenDevice = run {

            val numSufficientDevices = scores.count { score -> score.hasRequiredExtensions && score.hasRequiredFeatures && score.hasSuitableGraphicsQueue }
            if (numSufficientDevices == 0) {
                throw UnsupportedOperationException(
                    "No physical device (graphics card) has a presentation-ready graphics queue and all required extensions and features"
                )
            }
            if (numSufficientDevices == 1) {
                val chosenIndex = scores.indexOfFirst { score -> score.hasRequiredExtensions && score.hasSuitableGraphicsQueue }
                println("Chose ${scores[chosenIndex].name} because it is the only device with a presentation-ready graphics queue and all required extensions and features")
                return@run VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            }

            val numDiscreteDevices = scores.count { score -> score.typeScore == 2 }
            if (numDiscreteDevices == 1) {
                val chosenIndex = scores.indexOfFirst { score -> score.typeScore == 2 }
                println("Chose ${scores[chosenIndex].name} because it is the only discrete device")
                return@run VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            }

            val numIntegratedDevices = scores.count { score -> score.typeScore == 1 }
            if (numIntegratedDevices == 1) {
                val chosenIndex = scores.indexOfFirst { score -> score.typeScore == 1 }
                println("Chose ${scores[chosenIndex].name} because it is the only integrated device")
                return@run VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            }

            val maxMemorySize = scores.maxOf { score -> score.memorySize }
            val numMaxMemoryDevices = scores.count { score -> score.memorySize == maxMemorySize }
            val chosenIndex = scores.indexOfFirst { score -> score.memorySize == maxMemorySize }
            if (numMaxMemoryDevices == 1) {
                println("Chose ${scores[chosenIndex].name} because it has the biggest memory")
                VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            } else {
                println("Chose ${scores[chosenIndex].name} because it is the first listed device with biggest memory")
                VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            }
        }

        // So far, we only care about the required extensions, which must be available if we reach this line
        val chosenExtensions = requiredDeviceExtensions.toSet()
        appState.physicalDevice = chosenDevice
        appState.deviceExtensions = chosenExtensions
    }
}

fun isGraphicsQueueFamilySuitable(
    appState: ApplicationState, stack: MemoryStack,
    device: VkPhysicalDevice, familyIndex: Int, queueFamilyProps: VkQueueFamilyProperties
): Boolean {
    if (queueFamilyProps.queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
        if (glfwGetPhysicalDevicePresentationSupport(appState.instance, device, familyIndex)) {

            val pSupported = stack.callocInt(1)
            vkGetPhysicalDeviceSurfaceSupportKHR(device, familyIndex, appState.windowSurface!!, pSupported)

            if (pSupported[0] == VK_TRUE) {
                return true
            }
        }
    }

    return false
}
