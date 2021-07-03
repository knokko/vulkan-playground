package playground

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo
import org.lwjgl.vulkan.VkShaderModuleCreateInfo

fun createBasicShaders(appState: ApplicationState, stack: MemoryStack): VkPipelineShaderStageCreateInfo.Buffer {
    val ciShaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack)

    val vertexStage = ciShaderStages[0]
    vertexStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    vertexStage.flags(0)
    vertexStage.stage(VK_PIPELINE_STAGE_VERTEX_SHADER_BIT)
    vertexStage.module(createShaderModule(appState, stack, "vert", "basic"))
    vertexStage.pName(stack.UTF8("main"))

    val fragmentStage = ciShaderStages[1]
    fragmentStage.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
    fragmentStage.flags(0)
    fragmentStage.stage(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
    fragmentStage.module(createShaderModule(appState, stack, "frag", "basic"))
    fragmentStage.pName(stack.UTF8("main"))

    return ciShaderStages
}

fun createShaderModule(appState: ApplicationState, stack: MemoryStack, stage: String, name: String): Long {
    val vertexByteCode = mallocBundledResource("shaders/$name.$stage.spv")

    val ciShaderModule = VkShaderModuleCreateInfo.callocStack(stack)
    ciShaderModule.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
    ciShaderModule.pCode(vertexByteCode)

    val pShaderModule = stack.callocLong(1)
    assertSuccess(
        vkCreateShaderModule(appState.device, ciShaderModule, null, pShaderModule),
        "CreateShaderModule", stage
    )

    memFree(vertexByteCode)
    return pShaderModule[0]
}
