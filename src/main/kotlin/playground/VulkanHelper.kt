package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.vulkan.VK10.VK_TIMEOUT
import org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties
import org.lwjgl.vulkan.VkMemoryRequirements
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.util.*

fun assertSuccess(returnCode: Int, functionName: String, functionContext: String?) {
    // For some reason, VK_TIMEOUT is not considered a failure
    if (returnCode < 0 || returnCode == VK_TIMEOUT) {
        if (functionContext != null) {
            throw VulkanException("$functionName ($functionContext) returned $returnCode")
        } else {
            throw VulkanException("$functionName returned $returnCode")
        }
    }
}

fun assertSuccess(returnCode: Int, functionName: String) {
    assertSuccess(returnCode, functionName, null)
}

class VulkanException(message: String): RuntimeException(message)

fun mallocBundledResource(path: String): ByteBuffer {
    val stream = ApplicationState::class.java.classLoader.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Can't load resource $path")

    val array = stream.readBytes()
    stream.close()

    val buffer = memAlloc(array.size)
    buffer.put(array)
    buffer.position(0)

    return buffer
}

fun chooseMemoryTypeIndex(device: VkPhysicalDevice, memoryTypeBits: Int, requiredPropertyFlags: Int): Int? {
    stackPush().use { stack ->
        val memoryProps = VkPhysicalDeviceMemoryProperties.callocStack(stack)
        vkGetPhysicalDeviceMemoryProperties(device, memoryProps)

        for (memTypeIndex in 0 until memoryProps.memoryTypeCount()) {
            if (((1 shl memTypeIndex) and memoryTypeBits) != 0) {
                val availablePropertyFlags = memoryProps.memoryTypes(memTypeIndex).propertyFlags() and requiredPropertyFlags
                if (availablePropertyFlags == requiredPropertyFlags) {
                    return memTypeIndex
                }
            }
        }

        return null
    }
}
