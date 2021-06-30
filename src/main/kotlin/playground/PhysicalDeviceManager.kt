package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import java.nio.ByteBuffer

val requiredDeviceExtensions = arrayOf("VK_KHR_draw_indirect_count")

private class DeviceScore {
    lateinit var name: String
    // Initially true, but will be replaced with false if any extension is missing
    var hasRequiredExtensions = true
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
        }
        println()


        val numSufficientDevices = scores.count { score -> score.hasRequiredExtensions }
        if (numSufficientDevices == 0) {
            throw UnsupportedOperationException("No physical device (graphics card) has all required extensions")
        }

        if (numSufficientDevices == 1) {
            val chosenIndex = scores.indexOfFirst { score -> score.hasRequiredExtensions }
            println("Chose ${scores[chosenIndex].name} because it is the only device with all required extensions")
            appState.physicalDevice = VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            return
        }

        val numDiscreteDevices = scores.count { score -> score.typeScore == 2 }
        if (numDiscreteDevices == 1) {
            val chosenIndex = scores.indexOfFirst { score -> score.typeScore == 2 }
            println("Chose ${scores[chosenIndex].name} because it is the only discrete device")
            appState.physicalDevice = VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            return
        }

        val numIntegratedDevices = scores.count { score -> score.typeScore == 1 }
        if (numIntegratedDevices == 1) {
            val chosenIndex = scores.indexOfFirst { score -> score.typeScore == 1 }
            println("Chose ${scores[chosenIndex].name} because it is the only integrated device")
            appState.physicalDevice = VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
            return
        }

        val maxMemorySize = scores.maxOf { score -> score.memorySize }
        val numMaxMemoryDevices = scores.count { score -> score.memorySize == maxMemorySize }
        val chosenIndex = scores.indexOfFirst { score -> score.memorySize == maxMemorySize }
        if (numMaxMemoryDevices == 1) {
            println("Chose ${scores[chosenIndex].name} because it has the biggest memory")
            appState.physicalDevice = VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
        } else {
            println("Chose ${scores[chosenIndex].name} because it is the first listed device with biggest memory")
            appState.physicalDevice = VkPhysicalDevice(pDevices[chosenIndex], appState.instance)
        }
    }
}
