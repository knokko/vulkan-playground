package playground.image

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPhysicalDeviceLimits
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import org.lwjgl.vulkan.VkSamplerCreateInfo
import playground.ApplicationState
import playground.assertSuccess
import java.lang.Float.min

fun createImageSamplers(appState: ApplicationState) {
    stackPush().use { stack ->

        val deviceProps = VkPhysicalDeviceProperties.callocStack(stack)
        vkGetPhysicalDeviceProperties(appState.physicalDevice, deviceProps)
        val maxAnisotropy = deviceProps.limits().maxSamplerAnisotropy()

        // TODO Experiment with this value
        val chosenAnisotropy = min(maxAnisotropy, 3f)
        println("maxAnisotropy is $maxAnisotropy and chosen anisotropy is $chosenAnisotropy")

        val ciSampler = VkSamplerCreateInfo.callocStack(stack)
        ciSampler.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
        ciSampler.magFilter(VK_FILTER_LINEAR)
        ciSampler.minFilter(VK_FILTER_LINEAR)
        ciSampler.mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
        ciSampler.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
        ciSampler.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
        ciSampler.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
        // TODO Set mipLodBias when adding mipmapping
        ciSampler.anisotropyEnable(true)
        ciSampler.maxAnisotropy(chosenAnisotropy)
        ciSampler.compareEnable(false)
        ciSampler.minLod(0f)
        ciSampler.maxLod(0f)
        ciSampler.unnormalizedCoordinates(false)

        val pSampler = stack.callocLong(1)
        assertSuccess(
            vkCreateSampler(appState.device, ciSampler, null, pSampler),
            "CreateSampler"
        )
        appState.basicImageSampler = pSampler[0]
    }
}

fun destroyImageSamplers(appState: ApplicationState) {
    if (appState.basicImageSampler != null) {
        vkDestroySampler(appState.device, appState.basicImageSampler!!, null)
    }
}
