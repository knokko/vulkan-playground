package playground.command

import playground.ApplicationState

fun createCommandResources(appState: ApplicationState) {
    createCommandPools(appState)
    createCommandBuffers(appState)
}

fun destroyCommandResources(appState: ApplicationState) {
    destroyCommandBuffers(appState)
    destroyCommandPools(appState)
}
