package playground

import java.lang.Exception
import java.lang.Thread.sleep

const val TRY_DEBUG = true

fun main() {
    val appState = ApplicationState()
    try {
        initWindow(appState)
        initInstance(appState, TRY_DEBUG)
        choosePhysicalDevice(appState)
        initLogicalDevice(appState)

        openWindow(appState)
        while (!shouldCloseWindow(appState)) {
            sleep(16)
        }
        closeWindow(appState)
    } catch (trouble: Exception) {
        trouble.printStackTrace()
    }

    // Ensure that all resources are always destroyed
    destroyLogicalDevice(appState)
    destroyInstance(appState)
    destroyWindow(appState)
}
