package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo

fun createBasicViewportState(stack: MemoryStack): VkPipelineViewportStateCreateInfo {
    val ciViewport = VkPipelineViewportStateCreateInfo.callocStack(stack)
    ciViewport.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
    // We will use a dynamic viewport and scissor
    ciViewport.viewportCount(1)
    ciViewport.pViewports(null)
    ciViewport.scissorCount(1)
    ciViewport.pScissors(null)

    return ciViewport
}

fun createBasicRasterizationState(stack: MemoryStack): VkPipelineRasterizationStateCreateInfo {
    val ciRasterization = VkPipelineRasterizationStateCreateInfo.callocStack(stack)
    ciRasterization.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
    ciRasterization.depthClampEnable(false)
    ciRasterization.rasterizerDiscardEnable(false)
    ciRasterization.polygonMode(VK_POLYGON_MODE_FILL)
    // TODO Cull back after initial testing
    ciRasterization.cullMode(VK_CULL_MODE_NONE)
    ciRasterization.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
    ciRasterization.depthBiasEnable(false)
    ciRasterization.lineWidth(1f)

    return ciRasterization
}

fun createBasicMultisampleState(stack: MemoryStack): VkPipelineMultisampleStateCreateInfo {
    val ciMultisample = VkPipelineMultisampleStateCreateInfo.callocStack(stack)
    ciMultisample.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
    ciMultisample.rasterizationSamples(VK_SAMPLE_COUNT_4_BIT)
    ciMultisample.sampleShadingEnable(false)
    ciMultisample.pSampleMask(null)
    ciMultisample.alphaToCoverageEnable(false)
    ciMultisample.alphaToOneEnable(false)

    return ciMultisample
}
