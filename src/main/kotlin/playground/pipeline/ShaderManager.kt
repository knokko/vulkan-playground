package playground

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo
import org.lwjgl.vulkan.VkShaderModuleCreateInfo

fun createBasicShaders(appState: ApplicationState, stack: MemoryStack): VkPipelineShaderStageCreateInfo.Buffer {
    val ciShaderStages = VkPipelineShaderStageCreateInfo.callocStack(4, stack)

    val vertexStage = ciShaderStages[0]
    vertexStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    vertexStage.flags(0)
    vertexStage.stage(VK_SHADER_STAGE_VERTEX_BIT)
    vertexStage.module(createShaderModule(appState, stack, "vert", "basic"))
    vertexStage.pName(stack.UTF8("main"))

    val tessControlStage = ciShaderStages[1]
    tessControlStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    tessControlStage.flags(0)
    tessControlStage.stage(VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT)
    tessControlStage.module(createShaderModule(appState, stack, "tesc", "basic"))
    tessControlStage.pName(stack.UTF8("main"))

    val tessEvalStage = ciShaderStages[2]
    tessEvalStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    tessEvalStage.flags(0)
    tessEvalStage.stage(VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT)
    tessEvalStage.module(createShaderModule(appState, stack, "tese", "basic"))
    tessEvalStage.pName(stack.UTF8("main"))

    val fragmentStage = ciShaderStages[3]
    fragmentStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    fragmentStage.flags(0)
    fragmentStage.stage(VK_SHADER_STAGE_FRAGMENT_BIT)
    fragmentStage.module(createShaderModule(appState, stack, "frag", "basic"))
    fragmentStage.pName(stack.UTF8("main"))

    return ciShaderStages
}

fun createShaderModule(appState: ApplicationState, stack: MemoryStack, stage: String, name: String): Long {
    val shaderByteCode = mallocBundledResource("shaders/$name.$stage.spv")

    val ciShaderModule = VkShaderModuleCreateInfo.callocStack(stack)
    ciShaderModule.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
    ciShaderModule.pCode(shaderByteCode)

    val pShaderModule = stack.callocLong(1)
    assertSuccess(
        vkCreateShaderModule(appState.device, ciShaderModule, null, pShaderModule),
        "CreateShaderModule", stage
    )

    memFree(shaderByteCode)
    return pShaderModule[0]
}
