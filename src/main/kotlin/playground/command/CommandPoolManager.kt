package playground.command

import org.lwjgl.vulkan.VK10.vkDestroyCommandPool
import playground.ApplicationState

fun createCommandPools(appState: ApplicationState) {

}

fun createBufferCopyCommandPool(appState: ApplicationState) {

}

fun createStaticDrawingCommandPool(appState: ApplicationState) {

}

fun destroyCommandPools(appState: ApplicationState) {
    if (appState.bufferCopyCommandPool != null) {
        vkDestroyCommandPool(appState.device, appState.bufferCopyCommandPool!!, null)
    }
    if (appState.staticDrawCommandPool != null) {
        vkDestroyCommandPool(appState.device, appState.staticDrawCommandPool!!, null)
    }
}
