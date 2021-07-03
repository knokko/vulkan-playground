package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO
import org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo
import org.lwjgl.vulkan.VkVertexInputAttributeDescription
import org.lwjgl.vulkan.VkVertexInputBindingDescription
import playground.vertex.BasicVertex

fun createBasicVertexInputState(stack: MemoryStack) {
    val ciVertexInput = VkPipelineVertexInputStateCreateInfo.callocStack(stack)
    ciVertexInput.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)

    val bindings = VkVertexInputBindingDescription.callocStack(1, stack)
    val binding = bindings[0]
    binding.binding(0)
    binding.stride(BasicVertex.SIZE)
    binding.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)

    val attributes = VkVertexInputAttributeDescription.callocStack(1, stack)
}
