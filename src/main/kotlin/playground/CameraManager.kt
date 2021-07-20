package playground

import org.joml.Math.*
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.stackPush

private const val CAMERA_SPEED = 0.5f
fun updateCamera(appState: ApplicationState) {
    val camera = appState.camera

    camera.motionForward = 0f
    if (glfwGetKey(appState.window!!, GLFW_KEY_W) == GLFW_PRESS) {
        camera.motionForward += CAMERA_SPEED
    }
    if (glfwGetKey(appState.window!!, GLFW_KEY_S) == GLFW_PRESS) {
        camera.motionForward -= CAMERA_SPEED
    }

    camera.motionVertical = 0f
    if (glfwGetKey(appState.window!!, GLFW_KEY_SPACE) == GLFW_PRESS) {
        camera.motionVertical += CAMERA_SPEED
    }
    if (glfwGetKey(appState.window!!, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
        camera.motionVertical -= CAMERA_SPEED
    }

    camera.motionSide = 0f
    if (glfwGetKey(appState.window!!, GLFW_KEY_D) == GLFW_PRESS) {
        camera.motionSide += CAMERA_SPEED
    }
    if (glfwGetKey(appState.window!!, GLFW_KEY_A) == GLFW_PRESS) {
        camera.motionSide -= CAMERA_SPEED
    }

    val (mouseX, mouseY) = stackPush().use { stack ->
        val pMouseX = stack.callocDouble(1)
        val pMouseY = stack.callocDouble(1)
        glfwGetCursorPos(appState.window!!, pMouseX, pMouseY)
        Pair(pMouseX[0], pMouseY[0])
    }

    val oldMouseX = camera.oldMouseX
    val oldMouseY = camera.oldMouseY
    if (oldMouseX != null && oldMouseY != null && glfwGetMouseButton(appState.window!!, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
        camera.yaw += (mouseX - oldMouseX).toFloat()
        camera.pitch += (mouseY - oldMouseY).toFloat()
    }

    camera.oldMouseX = mouseX
    camera.oldMouseY = mouseY

    camera.updateMovement()
}

class Camera {

    var oldMouseX: Double? = null
    var oldMouseY: Double? = null

    var posX: Float = 0f
    var posY: Float = 10f
    var posZ: Float = 0f

    var motionForward: Float = 0f
    var motionVertical: Float = 0f
    var motionSide: Float = 0f

    var pitch: Float = 20f
    var yaw: Float = 0f

    val forwardDirection: Vector3f
    get() = Vector3f(sin(toRadians(yaw)), 0f, -cos(toRadians(yaw)))

    val sideDirection: Vector3f
    get() = Vector3f(cos(toRadians(yaw)), 0f, sin(toRadians(yaw)))

    fun updateMovement() {
        posX += forwardDirection.x * motionForward
        posX += sideDirection.x * motionSide
        posY += motionVertical
        posZ += forwardDirection.z * motionForward
        posZ += sideDirection.z * motionSide
    }
}
