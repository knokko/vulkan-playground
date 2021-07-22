package playground.pipeline

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo
import playground.ApplicationState
import playground.assertSuccess

fun createBasicPipelineLayout(appState: ApplicationState, stack: MemoryStack) {
    val bindings = VkDescriptorSetLayoutBinding.callocStack(4, stack)
    val ciCameraMatrix = bindings[0]
    ciCameraMatrix.binding(0)
    ciCameraMatrix.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
    ciCameraMatrix.descriptorCount(1)
    ciCameraMatrix.stageFlags(VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT or VK_SHADER_STAGE_FRAGMENT_BIT)

    val ciColorImage = bindings[1]
    ciColorImage.binding(1)
    ciColorImage.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
    ciColorImage.descriptorCount(1)
    ciColorImage.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)

    val ciHeightImage = bindings[2]
    ciHeightImage.binding(2)
    ciHeightImage.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
    ciHeightImage.descriptorCount(1)
    ciHeightImage.stageFlags(VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT or VK_SHADER_STAGE_FRAGMENT_BIT)

    val ciTransformationMatrices = bindings[3]
    ciTransformationMatrices.binding(3)
    ciTransformationMatrices.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
    ciTransformationMatrices.descriptorCount(1)
    ciTransformationMatrices.stageFlags(VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT or VK_SHADER_STAGE_FRAGMENT_BIT)

    val ciSetLayouts = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
    ciSetLayouts.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
    ciSetLayouts.pBindings(bindings)

    val pSetLayouts = stack.callocLong(1)
    assertSuccess(
        vkCreateDescriptorSetLayout(appState.device, ciSetLayouts, null, pSetLayouts),
        "CreateDescriptorSetLayout", "basic"
    )
    appState.basicDescriptorSetLayout = pSetLayouts[0]

    val ciLayout = VkPipelineLayoutCreateInfo.callocStack(stack)
    ciLayout.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
    ciLayout.pSetLayouts(pSetLayouts)
    ciLayout.pPushConstantRanges(null)

    val pLayout = stack.callocLong(1)
    assertSuccess(
        vkCreatePipelineLayout(appState.device, ciLayout, null, pLayout),
        "CreatePipelineLayout", "basic"
    )
    appState.basicPipelineLayout = pLayout[0]
}

fun destroyBasicPipelineLayout(appState: ApplicationState) {
    if (appState.basicPipelineLayout != null) {
        vkDestroyPipelineLayout(appState.device, appState.basicPipelineLayout!!, null)
    }
    if (appState.basicDescriptorSetLayout != null) {
        vkDestroyDescriptorSetLayout(appState.device, appState.basicDescriptorSetLayout!!, null)
    }
}
