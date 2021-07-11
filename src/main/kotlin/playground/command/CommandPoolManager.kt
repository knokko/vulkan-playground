package playground.command

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkCommandPoolCreateInfo
import playground.ApplicationState
import playground.assertSuccess

fun createCommandPools(appState: ApplicationState) {
    createStaticDrawingCommandPool(appState)
    createBufferCopyCommandPool(appState)
}

fun createBufferCopyCommandPool(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciCopyPool = VkCommandPoolCreateInfo.callocStack(stack)
        ciCopyPool.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
        ciCopyPool.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT)
        ciCopyPool.queueFamilyIndex(appState.queueFamilyIndex!!)

        val pCopyPool = stack.callocLong(1)
        assertSuccess(
            vkCreateCommandPool(appState.device, ciCopyPool, null, pCopyPool),
            "CreateCommandPool", "copy"
        )
        appState.bufferCopyCommandPool = pCopyPool[0]
    }
}

fun createStaticDrawingCommandPool(appState: ApplicationState) {
    stackPush().use { stack ->
        val ciDrawPool = VkCommandPoolCreateInfo.callocStack(stack)
        ciDrawPool.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
        ciDrawPool.queueFamilyIndex(appState.queueFamilyIndex!!)

        val pDrawPool = stack.callocLong(1)
        assertSuccess(
            vkCreateCommandPool(appState.device, ciDrawPool, null, pDrawPool),
            "CreateCommandPool", "static draw"
        )
        appState.staticDrawCommandPool = pDrawPool[0]
    }
}

fun destroyCommandPools(appState: ApplicationState) {
    if (appState.bufferCopyCommandPool != null) {
        vkDestroyCommandPool(appState.device, appState.bufferCopyCommandPool!!, null)
    }
    if (appState.staticDrawCommandPool != null) {
        vkDestroyCommandPool(appState.device, appState.staticDrawCommandPool!!, null)
    }
}
