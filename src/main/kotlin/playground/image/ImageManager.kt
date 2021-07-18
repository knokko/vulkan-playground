package playground.image

import playground.ApplicationState

fun createResolutionDependantImageResources(appState: ApplicationState) {
    createColorImage(appState)
    createDepthImage(appState)
    allocateResolutionDependantImageMemory(appState)
    createSwapchainImageViews(appState)
    createColorImageView(appState)
    createDepthImageView(appState)
}

fun destroyResolutionDependantImageResources(appState: ApplicationState) {
    destroyColorImageView(appState)
    destroyDepthImageView(appState)
    destroySwapchainImageViews(appState)
    destroyColorImage(appState)
    destroyDepthImage(appState)
    freeResolutionDependantImageMemory(appState)
}
