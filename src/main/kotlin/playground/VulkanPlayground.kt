package playground

const val TRY_DEBUG = true

fun main() {
    val appState = ApplicationState()
    initInstance(appState, TRY_DEBUG)
    choosePhysicalDevice(appState)
    initLogicalDevice(appState)
    destroyLogicalDevice(appState)
    destroyInstance(appState)
}
