package playground

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
        createGraphicsPipelines(appState)

        openWindow(appState)
        while (!shouldCloseWindow(appState)) {
            sleep(16)
        }
        closeWindow(appState)
    } catch (trouble: Exception) {
        trouble.printStackTrace()
    }

    // Ensure that all resources are always destroyed
    destroyResolutionDependantImageResources(appState)
    destroyGraphicsPipelines(appState)
    destroyRenderPasses(appState)
    destroySwapchain(appState)
    destroyLogicalDevice(appState)
    destroyWindowSurface(appState)
    destroyInstance(appState)
    destroyWindow(appState)
}
