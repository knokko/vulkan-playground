package playground

import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT
import org.lwjgl.vulkan.VK10.vkDestroyDevice
import org.lwjgl.vulkan.VK10.vkDestroyInstance
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkPhysicalDevice

class ApplicationState {

    var window: Long? = null

    lateinit var instance: VkInstance
    var debugCallback: Long? = null

    lateinit var physicalDevice: VkPhysicalDevice
    lateinit var deviceExtensions: Set<String>
    lateinit var device: VkDevice
    var queueFamilyIndex: Int? = null

    fun destroyWindow() {
        if (window != null) {
            glfwDestroyWindow(window!!)
        }
    }

    fun destroyInstance() {
        if (this::instance.isInitialized) {
            vkDestroyInstance(instance, null)
        }
    }

    fun destroyDebugCallback() {
        if (debugCallback != null) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugCallback!!, null)
        }
    }

    fun destroyDevice() {
        if (this::device.isInitialized) {
            vkDestroyDevice(device, null)
        }
    }
}