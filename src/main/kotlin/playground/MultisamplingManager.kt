package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkPhysicalDeviceProperties

fun determineSampleCount(appState: ApplicationState) {
    stackPush().use { stack ->
        val deviceProperties = VkPhysicalDeviceProperties.callocStack(stack)
        vkGetPhysicalDeviceProperties(appState.physicalDevice, deviceProperties)

        val limits = deviceProperties.limits()
        println("Sample count framebuffer support:")
        println("color: ${limits.framebufferColorSampleCounts()}")
        println("depth: ${limits.framebufferDepthSampleCounts()}")

        // We need to watch the stencil component as well because we might end up using a 24depth8stencil format to
        // avoid the cost of 32depth format. Not that it matters since stencil sample support is almost always at
        // least as good as the support for color and depth samples.
        println("stencil: ${limits.framebufferStencilSampleCounts()}")
        val combined = limits.framebufferColorSampleCounts() and limits.framebufferDepthSampleCounts() and limits.framebufferStencilSampleCounts()
        println("combined: $combined")

        // TODO Experiment with comparison between 2, 4, and 8
        val preferredSampleCounts = arrayOf(VK_SAMPLE_COUNT_8_BIT, VK_SAMPLE_COUNT_4_BIT, VK_SAMPLE_COUNT_2_BIT)
        appState.sampleCount = run {
            for (candidate in preferredSampleCounts) {
                if ((candidate and combined) != 0) {
                    return@run candidate
                }
            }

            // This shouldn't happen with any real device, so this line being reached indicates a bug somewhere
            throw UnsupportedOperationException("This physical device doesn't support any sample count > 1")
        }
    }
}