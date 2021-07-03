package playground

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo

fun createGraphicsPipelines(appState: ApplicationState) {
    stackPush().use {stack ->
        val ciPipelines = VkGraphicsPipelineCreateInfo.callocStack(1, stack)
        createBasicGraphicsPipeline(appState, ciPipelines[0], stack)
        val pPipelines = stack.callocLong(ciPipelines.capacity())
        assertSuccess(
            vkCreateGraphicsPipelines(appState.device, VK_NULL_HANDLE, ciPipelines, null, pPipelines),
            "CreateGraphicsPipelines"
        )
        appState.basicPipeline = pPipelines[0]
    }
}

fun createBasicGraphicsPipeline(appState: ApplicationState, ciPipeline: VkGraphicsPipelineCreateInfo, stack: MemoryStack) {
    // TODO Fill in
    ciPipeline.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
    ciPipeline.flags(0)
    ciPipeline.pStages(createBasicShaders(appState, stack))

    // TODO Destroy shader modules
}

fun destroyGraphicsPipelines(appState: ApplicationState) {
    if (appState.basicPipeline != null) {
        vkDestroyPipeline(appState.device, appState.basicPipeline!!, null)
    }
}
