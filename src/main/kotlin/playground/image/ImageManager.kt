package playground.image

import playground.ApplicationState

fun createResolutionDependantImageResources(appState: ApplicationState) {
    createDepthImage(appState)
    allocateResolutionDependantImageMemory(appState)
    createSwapchainImageViews(appState)
    createDepthImageView(appState)
    createFramebuffers(appState)
}

fun destroyResolutionDependantImageResources(appState: ApplicationState) {
    destroyFramebuffers(appState)
    destroyDepthImageView(appState)
    destroySwapchainImageViews(appState)
    destroyDepthImage(appState)
    freeResolutionDependantImageMemory(appState)
}
