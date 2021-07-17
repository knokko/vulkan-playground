package playground

import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import java.nio.ByteBuffer

class ApplicationState {

    var window: Long? = null
    var windowSurface: Long? = null
    var swapchain: Long? = null
    lateinit var swapchainImages: Array<SwapchainImage>
    var swapchainColorFormat: Int? = null
    var swapchainWidth: Int? = null
    var swapchainHeight: Int? = null

    var depthImage: Long? = null
    var depthImageView: Long? = null
    var depthFormat: Int? = null
    var resolutionDependantMemory: Long? = null

    lateinit var instance: VkInstance
    var debugCallback: Long? = null

    lateinit var physicalDevice: VkPhysicalDevice
    lateinit var deviceExtensions: Set<String>
    lateinit var device: VkDevice
    var queueFamilyIndex: Int? = null
    lateinit var graphicsQueue: VkQueue

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
    lateinit var uniformData: ByteBuffer
    var storageBuffer: Long? = null
    var storageMemory: Long? = null
    lateinit var storageData: ByteBuffer

    var vertexBuffer: Long? = null
    var vertexMemory: Long? = null
    var indexBuffer: Long? = null
    var indexMemory: Long? = null

    var bufferCopyCommandPool: Long? = null
    var staticDrawCommandPool: Long? = null

    var indirectDrawBuffer: Long? = null
    var indirectMemory: Long? = null
    lateinit var indirectDrawData: ByteBuffer
    var indirectDrawOffset: Long? = null
    var indirectCountOffset: Int? = null

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
        if (this::swapchainImages.isInitialized) {
            for (swapchainImage in swapchainImages) {
                if (swapchainImage.view != null) {
                    vkDestroyImageView(device, swapchainImage.view!!, null)
                }
            }
        }
    }

    fun destroyFramebuffers() {
        if (this::swapchainImages.isInitialized) {
            for (swapchainImage in swapchainImages) {
                if (swapchainImage.framebuffer != null) {
                    vkDestroyFramebuffer(device, swapchainImage.framebuffer!!, null)
                }
            }
        }
    }

    fun destroyStaticDrawCommandBuffers() {
        if (this::swapchainImages.isInitialized) {
            for (swapchainImage in swapchainImages) {
                swapchainImage.destroyStaticDrawCommandBuffer(device, staticDrawCommandPool!!)
            }
        }
    }

    fun waitGraphicsQueue() {
        if (this::graphicsQueue.isInitialized) {
            assertSuccess(
                vkQueueWaitIdle(graphicsQueue), "QueueWaitIdle", "destroy graphics"
            )
        }
    }

    fun hasIndirectDrawData(): Boolean {
        return this::indirectDrawData.isInitialized
    }

    fun hasSwapchainImages(): Boolean {
        return this::swapchainImages.isInitialized
    }
}
