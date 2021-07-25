package playground

import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memUTF8
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.VK10.*
import java.nio.ByteBuffer

const val VK_LAYER_KHRONOS_VALIDATION_NAME = "VK_LAYER_KHRONOS_validation"

fun initInstance(appState: ApplicationState, tryDebug: Boolean) {
    if (!GLFWVulkan.glfwVulkanSupported()) {
        throw UnsupportedOperationException("No GLFW ~ Vulkan support")
    }

    stackPush().use { stack ->
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
        val pRequiredExtensions = glfwGetRequiredInstanceExtensions()
            ?: throw RuntimeException("No suitable GLFW Vulkan instance extensions found")

        if (tryDebug && availableExtensions.contains(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)) {
            chosenExtensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        }
        for (index in 0 until pRequiredExtensions.capacity()) {
            val extension = memUTF8(pRequiredExtensions[index])
            if (availableExtensions.contains(extension)) {
                chosenExtensions.add(extension)
            } else {
                throw RuntimeException("The required instance extension $extension (required by GLFW) is not available")
            }
        }
        if (appState.useVR!!) {
            addVrInstanceExtensions(chosenExtensions)
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

        val appInfo = VkApplicationInfo.callocStack(stack)
        appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
        appInfo.pApplicationName(stack.UTF8("Vulkan Playground"))
        appInfo.applicationVersion(1)
        appInfo.apiVersion(VK_MAKE_VERSION(1, 2, 0))

        val ciInstance = VkInstanceCreateInfo.callocStack(stack)
        ciInstance.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
        ciInstance.pApplicationInfo(appInfo)
        ciInstance.ppEnabledExtensionNames(pChosenExtensions)
        ciInstance.ppEnabledLayerNames(pChosenLayers)

        val pInstance = stack.callocPointer(1)
        assertSuccess(vkCreateInstance(ciInstance, null, pInstance), "createInstance")
        appState.instance = VkInstance(pInstance.get(0), ciInstance)

        if (tryDebug) {
            val ciDebugMessenger = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
            ciDebugMessenger.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
            ciDebugMessenger.messageSeverity(
                        VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or
                                VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT
            )
            ciDebugMessenger.messageType(
                VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
            )
            ciDebugMessenger.pfnUserCallback { _, _, rawData, _->
                val data = VkDebugUtilsMessengerCallbackDataEXT.create(rawData)
                println("VulkanDebugCallback: ${data.pMessageString()}")
                VK_FALSE
            }

            val pDebugMessenger = stack.callocLong(1)
            assertSuccess(
                vkCreateDebugUtilsMessengerEXT(appState.instance, ciDebugMessenger, null, pDebugMessenger),
                "CreateDebugUtilsMessengerEXT"
            )
            appState.debugCallback = pDebugMessenger[0]
        }
    }
}

fun destroyInstance(appState: ApplicationState) {
    if (appState.debugCallback != null) {
        vkDestroyDebugUtilsMessengerEXT(appState.instance, appState.debugCallback!!, null)
    }
    appState.destroyInstance()
}
