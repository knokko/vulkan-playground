package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo
import org.lwjgl.vulkan.VkVertexInputAttributeDescription
import org.lwjgl.vulkan.VkVertexInputBindingDescription
import playground.vertex.BasicVertex

fun createBasicVertexInputState(stack: MemoryStack): VkPipelineVertexInputStateCreateInfo {

    val bindings = VkVertexInputBindingDescription.callocStack(1, stack)
    val binding = bindings[0]
    binding.binding(0)
    binding.stride(BasicVertex.SIZE)
    binding.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)

    val attributes = VkVertexInputAttributeDescription.callocStack(4, stack)
    val attributePosition = attributes[0]
    attributePosition.location(0)
    attributePosition.binding(0)
    attributePosition.format(VK_FORMAT_R32G32B32_SFLOAT)
    attributePosition.offset(BasicVertex.OFFSET_BASE_POSITION)
    val attributeNormal = attributes[1]
    attributeNormal.location(1)
    attributeNormal.binding(0)
    attributeNormal.format(VK_FORMAT_R32G32B32_SFLOAT)
    attributeNormal.offset(BasicVertex.OFFSET_BASE_NORMAL)
    val attributeTexCoordinates = attributes[2]
    attributeTexCoordinates.location(2)
    attributeTexCoordinates.binding(0)
    attributeTexCoordinates.format(VK_FORMAT_R32G32_SFLOAT)
    attributeTexCoordinates.offset(BasicVertex.OFFSET_TEXTURE_COORDINATES)
    val attributeMatrixIndex = attributes[3]
    attributeMatrixIndex.location(3)
    attributeMatrixIndex.binding(0)
    attributeMatrixIndex.format(VK_FORMAT_R32_UINT)
    attributeMatrixIndex.offset(BasicVertex.OFFSET_MATRIX_INDEX)

    val ciVertexInput = VkPipelineVertexInputStateCreateInfo.callocStack(stack)
    ciVertexInput.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
    ciVertexInput.pVertexBindingDescriptions(bindings)
    ciVertexInput.pVertexAttributeDescriptions(attributes)

    return ciVertexInput
}

fun createBasicInputAssembly(stack: MemoryStack): VkPipelineInputAssemblyStateCreateInfo {
    val ciAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack)
    ciAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
    ciAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
    ciAssembly.primitiveRestartEnable(false)

    return ciAssembly
}
