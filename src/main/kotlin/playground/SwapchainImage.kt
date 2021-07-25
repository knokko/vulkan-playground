package playground

import org.lwjgl.vulkan.VK10.vkFreeCommandBuffers
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkDevice

class SwapchainImage(val image: Long, val renderSemaphore: Long, val preparePresentSemaphore: Long) {

    var view: Long? = null
    var framebuffer: Long? = null
    lateinit var staticDrawCommandBuffer: VkCommandBuffer
    lateinit var preparePresentCommandBuffer: VkCommandBuffer

    fun destroyStaticDrawCommandBuffer(device: VkDevice, commandPool: Long?) {
        if (this::staticDrawCommandBuffer.isInitialized) {
            vkFreeCommandBuffers(device, commandPool!!, staticDrawCommandBuffer)
        }
    }

    fun destroyPreparePresentCommandBuffer(device: VkDevice, commandPool: Long?) {
        if (this::preparePresentCommandBuffer.isInitialized) {
            vkFreeCommandBuffers(device, commandPool!!, preparePresentCommandBuffer)
        }
    }
}
