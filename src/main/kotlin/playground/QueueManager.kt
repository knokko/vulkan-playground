package playground

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.vkGetDeviceQueue
import org.lwjgl.vulkan.VkQueue

fun getQueues(appState: ApplicationState) {
    stackPush().use { stack ->
        val pGraphicsQueue = stack.callocPointer(1)
        vkGetDeviceQueue(appState.device, appState.queueFamilyIndex!!, 0, pGraphicsQueue)
        appState.graphicsQueue = VkQueue(pGraphicsQueue[0], appState.device)
    }
}

fun waitQueues(appState: ApplicationState) {
    appState.waitGraphicsQueue()
}
