package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo

fun createBasicDepthStencilState(stack: MemoryStack): VkPipelineDepthStencilStateCreateInfo {
    val ciDepthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
    ciDepthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
    ciDepthStencil.depthTestEnable(true)
    ciDepthStencil.depthWriteEnable(true)
    ciDepthStencil.depthCompareOp(VK_COMPARE_OP_LESS)
    ciDepthStencil.depthBoundsTestEnable(false)
    ciDepthStencil.stencilTestEnable(false)

    return ciDepthStencil
}
