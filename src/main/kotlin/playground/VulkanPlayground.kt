package playground

import playground.command.createCommandResources
import playground.command.destroyCommandResources
import playground.image.*
import playground.pipeline.createGraphicsPipelines
import playground.pipeline.destroyBasicPipelineLayout
import playground.pipeline.destroyGraphicsPipelines
import java.lang.Exception
import java.lang.Thread.sleep

const val TRY_DEBUG = true

fun main() {
    val appState = ApplicationState()
    try {
        initWindow(appState)
        initInstance(appState, TRY_DEBUG)
        initWindowSurface(appState)
        choosePhysicalDevice(appState)
        initLogicalDevice(appState)
        createSwapchain(appState)
        createResolutionDependantImageResources(appState)
        createRenderPasses(appState)
        createFramebuffers(appState)
        createGraphicsPipelines(appState)
        createDescriptorSets(appState)
        createIndirectDrawBuffer(appState)
        createCommandResources(appState)

        openWindow(appState)
        while (!shouldCloseWindow(appState)) {
            sleep(16)
        }
        closeWindow(appState)
    } catch (trouble: Exception) {
        trouble.printStackTrace()
    }

    // Ensure that all resources are always destroyed
    destroyCommandResources(appState)
    destroyIndirectDrawBuffer(appState)
    destroyDescriptorSets(appState)
    destroyResolutionDependantImageResources(appState)
    destroyGraphicsPipelines(appState)
    destroyFramebuffers(appState)
    destroyRenderPasses(appState)
    destroySwapchain(appState)
    destroyLogicalDevice(appState)
    destroyWindowSurface(appState)
    destroyInstance(appState)
    destroyWindow(appState)
}
