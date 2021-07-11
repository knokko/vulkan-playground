package playground.image

import playground.ApplicationState

fun createResolutionDependantImageResources(appState: ApplicationState) {
    createDepthImage(appState)
    allocateResolutionDependantImageMemory(appState)
    createSwapchainImageViews(appState)
    createDepthImageView(appState)
}

fun destroyResolutionDependantImageResources(appState: ApplicationState) {
    destroyDepthImageView(appState)
    destroySwapchainImageViews(appState)
    destroyDepthImage(appState)
    freeResolutionDependantImageMemory(appState)
}
