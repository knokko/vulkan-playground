package playground

import playground.command.*
import playground.image.*
import playground.pipeline.createGraphicsPipelines
import playground.pipeline.destroyGraphicsPipelines
import playground.vertex.createVertexBuffers
import playground.vertex.destroyVertexBuffers
import playground.vertex.fillVertexBuffers
import java.lang.Exception

const val TRY_DEBUG = true

fun main() {
    val appState = ApplicationState()
    try {
        initWindow(appState)
        initInstance(appState, TRY_DEBUG)
        initWindowSurface(appState)
        choosePhysicalDevice(appState)
        initLogicalDevice(appState)
        determineSampleCount(appState)
        createSwapchain(appState)
        createResolutionDependantImageResources(appState)
        createRenderPasses(appState)
        createFramebuffers(appState)
        createGraphicsPipelines(appState)
        createDescriptorSets(appState)
        createIndirectDrawBuffer(appState)
        createBufferCopyCommandPool(appState)
        createVertexBuffers(appState)
        createStaticDrawingCommandPool(appState)
        createStaticDrawCommandBuffers(appState)
        getQueues(appState)
        fillVertexBuffers(appState)

        openWindow(appState)
        while (!shouldCloseWindow(appState)) {
            drawFrame(appState)
        }
        closeWindow(appState)
    } catch (trouble: Exception) {
        trouble.printStackTrace()
    }

    // Ensure that all resources are always destroyed
    waitQueues(appState)
    destroyStaticDrawCommandBuffers(appState)
    destroyCommandPools(appState)
    destroyVertexBuffers(appState)
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
