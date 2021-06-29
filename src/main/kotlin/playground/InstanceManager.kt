package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import org.lwjgl.vulkan.VkLayerProperties
import java.nio.ByteBuffer

const val VK_LAYER_KHRONOS_VALIDATION_NAME = "VK_LAYER_KHRONOS_validation"
class InstanceManager(tryDebug: Boolean) {

    private val instance: VkInstance

    init {
        stackPush().use {stack ->
            val pNumAvailableExtensions = stack.callocInt(1)
            assertSuccess(
                vkEnumerateInstanceExtensionProperties(null as ByteBuffer?, pNumAvailableExtensions, null),
                "EnumerateInstanceExtensionProperties", "count"
            )
            val numAvailableExtensions = pNumAvailableExtensions[0]

            val pAvailableExtensions = VkExtensionProperties.callocStack(numAvailableExtensions, stack)
            assertSuccess(
                vkEnumerateInstanceExtensionProperties(null as ByteBuffer?, pNumAvailableExtensions, pAvailableExtensions),
                "EnumerateInstanceExtensionProperties", "extensions"
            )

            val availableExtensions = HashSet<String>()
            for (extensionIndex in 0 until numAvailableExtensions) {
                availableExtensions.add(pAvailableExtensions[extensionIndex].extensionNameString())
            }

            println("There are $numAvailableExtensions available instance extensions:")
            for (extension in availableExtensions) {
                println(extension)
            }
            println()

            val chosenExtensions = HashSet<String>()
            if (tryDebug && availableExtensions.contains(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)) {
                chosenExtensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
            }
            val numChosenExtensions = chosenExtensions.size

            val pChosenExtensions = stack.callocPointer(numChosenExtensions)
            for ((index, extension) in chosenExtensions.withIndex()) {
                pChosenExtensions.put(index, stack.UTF8(extension))
            }

            val pNumAvailableLayers = stack.callocInt(1)
            assertSuccess(
                vkEnumerateInstanceLayerProperties(pNumAvailableLayers, null),
                "EnumerateInstanceLayerProperties", "count"
            )
            val numAvailableLayers = pNumAvailableLayers[0]
            val pAvailableLayers = VkLayerProperties.callocStack(numAvailableLayers, stack)
            assertSuccess(
                vkEnumerateInstanceLayerProperties(pNumAvailableLayers, pAvailableLayers),
                "EnumerateInstanceLayerProperties", "layers"
            )
            val availableLayers = HashSet<String>()
            for (index in 0 until numAvailableLayers) {
                availableLayers.add(pAvailableLayers[index].layerNameString())
            }

            println("There are $numAvailableLayers available instance layers:")
            for (layer in availableLayers) {
                println(layer)
            }
            println()

            val chosenLayers = HashSet<String>()
            if (tryDebug && availableLayers.contains(VK_LAYER_KHRONOS_VALIDATION_NAME)) {
                chosenLayers.add(VK_LAYER_KHRONOS_VALIDATION_NAME)
            }
            val numChosenLayers = chosenLayers.size

            val pChosenLayers = stack.callocPointer(numChosenLayers)
            for ((index, layer) in chosenLayers.withIndex()) {
                pChosenLayers.put(index, stack.UTF8(layer))
            }

            println("Enabling $numChosenExtensions instance extensions:")
            for (extension in chosenExtensions) {
                println(extension)
            }
            println()

            println("Enabling $numChosenLayers instance layers:")
            for (layer in chosenLayers) {
                println(layer)
            }
            println()

            val ciInstance = VkInstanceCreateInfo.callocStack(stack)
            ciInstance.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            ciInstance.ppEnabledExtensionNames(pChosenExtensions)
            ciInstance.ppEnabledLayerNames(pChosenLayers)

            val pInstance = stack.callocPointer(1)
            assertSuccess(vkCreateInstance(ciInstance, null, pInstance), "createInstance")
            this.instance = VkInstance(pInstance.get(0), ciInstance)
        }
    }

    fun run() {

    }

    fun destroy() {
        vkDestroyInstance(this.instance, null)
    }
}
