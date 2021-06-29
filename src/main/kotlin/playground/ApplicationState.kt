package playground

import org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT
import org.lwjgl.vulkan.VK10.vkDestroyInstance
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT
import org.lwjgl.vulkan.VkInstance

class ApplicationState {

    lateinit var instance: VkInstance
    var debugCallback: Long? = null

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
}