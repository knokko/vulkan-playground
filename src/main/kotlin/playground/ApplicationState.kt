package playground

import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT
import org.lwjgl.vulkan.VK10.*

class ApplicationState {

    var window: Long? = null
    var windowSurface: Long? = null
    var swapchain: Long? = null
    lateinit var swapchainImages: Array<Long>
    lateinit var swapchainImageViews: Array<Long>
    var swapchainColorFormat: Int? = null
    var swapchainWidth: Int? = null
    var swapchainHeight: Int? = null

    var depthImage: Long? = null
    var depthImageView: Long? = null
    var depthFormat: Int? = null
    var resolutionDependantMemory: Long? = null

    lateinit var framebuffers: Array<Long>

    lateinit var instance: VkInstance
    var debugCallback: Long? = null

    lateinit var physicalDevice: VkPhysicalDevice
    lateinit var deviceExtensions: Set<String>
    lateinit var device: VkDevice
    var queueFamilyIndex: Int? = null

    // TODO Experiment with this later (and check physical device limits)
    val sampleCount = VK_SAMPLE_COUNT_1_BIT

    var basicRenderPass: Long? = null
    var basicPipeline: Long? = null
    var basicPipelineLayout: Long? = null

    var basicDescriptorSetLayout: Long? = null
    var basicDescriptorPool: Long? = null
    var basicDescriptorSet: Long? = null

    var uniformBuffer: Long? = null
    var uniformMemory: Long? = null
    var storageBuffer: Long? = null
    var storageMemory: Long? = null

    var bufferCopyCommandPool: Long? = null
    var staticDrawCommandPool: Long? = null

    lateinit var staticDrawCommandBuffers: Array<VkCommandBuffer>

    var indirectDrawBuffer: Long? = null
    var indirectMemory: Long? = null
    var indirectDrawOffset: Long? = null
    var indirectCountOffset: Long? = null

    fun destroyInstance() {
        if (this::instance.isInitialized) {
            vkDestroyInstance(instance, null)
        }
    }

    fun destroyDevice() {
        if (this::device.isInitialized) {
            vkDestroyDevice(device, null)
        }
    }

    fun destroySwapchainImageViews() {
        if (this::swapchainImageViews.isInitialized) {
            for (swapchainImageView in swapchainImageViews) {
                vkDestroyImageView(device, swapchainImageView, null)
            }
        }
    }

    fun destroyFramebuffers() {
        if (this::framebuffers.isInitialized) {
            for (framebuffer in framebuffers) {
                vkDestroyFramebuffer(device, framebuffer, null)
            }
        }
    }

    fun destroyStaticDrawCommandBuffers() {
        if (this::staticDrawCommandBuffers.isInitialized) {
            for (buffer in staticDrawCommandBuffers) {
                vkFreeCommandBuffers(device, staticDrawCommandPool!!, buffer)
            }
        }
    }
}
