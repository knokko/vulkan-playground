package playground

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.lang.RuntimeException

fun initWindow(appState: ApplicationState) {
    stackPush().use {stack ->
        if (!glfwInit()) {
            throw RuntimeException("Failed to initialize GLFW")
        }

        // Don't show the window until rendering is ready
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        appState.window = glfwCreateWindow(1000, 700, stack.UTF8("Vulkan playground"), NULL, NULL)
    }
}

fun initWindowSurface(appState: ApplicationState) {
    stackPush().use {stack ->
        val pSurface = stack.callocLong(1)
        assertSuccess(
            glfwCreateWindowSurface(appState.instance, appState.window!!, null, pSurface),
            "CreateWindowSurface"
        )
        appState.windowSurface = pSurface[0]
    }
}

fun openWindow(appState: ApplicationState) {
    glfwShowWindow(appState.window!!)
}

fun closeWindow(appState: ApplicationState) {
    glfwHideWindow(appState.window!!)
}

fun shouldCloseWindow(appState: ApplicationState): Boolean {
    glfwPollEvents()
    return glfwWindowShouldClose(appState.window!!)
}

fun destroyWindow(appState: ApplicationState) {
    if (appState.window != null) {
        glfwDestroyWindow(appState.window!!)
    }
    glfwTerminate()
}
