package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo
import playground.ApplicationState
import playground.assertSuccess
import playground.createBasicShaders

fun createGraphicsPipelines(appState: ApplicationState) {
    stackPush().use {stack ->

        createBasicPipelineLayout(appState, stack)

        val ciPipelines = VkGraphicsPipelineCreateInfo.callocStack(1, stack)
        createBasicGraphicsPipeline(appState, ciPipelines[0], stack)

        val pPipelines = stack.callocLong(ciPipelines.capacity())
        assertSuccess(
            vkCreateGraphicsPipelines(appState.device, VK_NULL_HANDLE, ciPipelines, null, pPipelines),
            "CreateGraphicsPipelines"
        )
        appState.basicPipeline = pPipelines[0]

        vkDestroyShaderModule(appState.device, ciPipelines[0].pStages()[0].module(), null)
        vkDestroyShaderModule(appState.device, ciPipelines[0].pStages()[1].module(), null)
        vkDestroyShaderModule(appState.device, ciPipelines[0].pStages()[2].module(), null)
        vkDestroyShaderModule(appState.device, ciPipelines[0].pStages()[3].module(), null)
    }
}

fun createBasicGraphicsPipeline(appState: ApplicationState, ciPipeline: VkGraphicsPipelineCreateInfo, stack: MemoryStack) {
    ciPipeline.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
    ciPipeline.flags(0)
    ciPipeline.pStages(createBasicShaders(appState, stack))
    ciPipeline.pVertexInputState(createBasicVertexInputState(stack))
    ciPipeline.pInputAssemblyState(createBasicInputAssembly(stack))
    ciPipeline.pTessellationState(createTessellationState(stack))
    ciPipeline.pViewportState(createBasicViewportState(stack))
    ciPipeline.pRasterizationState(createBasicRasterizationState(stack))
    ciPipeline.pMultisampleState(createBasicMultisampleState(appState, stack))
    ciPipeline.pDepthStencilState(createBasicDepthStencilState(stack))
    ciPipeline.pColorBlendState(createBasicColorBlendState(stack))
    ciPipeline.pDynamicState(createBasicDynamicState(stack))
    ciPipeline.layout(appState.basicPipelineLayout!!)
    ciPipeline.renderPass(appState.basicRenderPass!!)
    ciPipeline.subpass(0)
}

fun createTessellationState(stack: MemoryStack): VkPipelineTessellationStateCreateInfo {
    val ciTess = VkPipelineTessellationStateCreateInfo.callocStack(stack)
    ciTess.sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO)
    ciTess.patchControlPoints(3)

    return ciTess
}

fun createBasicDynamicState(stack: MemoryStack): VkPipelineDynamicStateCreateInfo {
    val ciDynamic = VkPipelineDynamicStateCreateInfo.callocStack(stack)
    ciDynamic.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
    ciDynamic.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR))

    return ciDynamic
}

fun destroyGraphicsPipelines(appState: ApplicationState) {
    if (appState.basicPipeline != null) {
        vkDestroyPipeline(appState.device, appState.basicPipeline!!, null)
    }
    destroyBasicPipelineLayout(appState)
}
