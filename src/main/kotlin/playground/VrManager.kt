package playground

import org.lwjgl.openvr.OpenVR
import org.lwjgl.openvr.Texture
import org.lwjgl.openvr.TrackedDevicePose
import org.lwjgl.openvr.VR.*
import org.lwjgl.openvr.VRCompositor.*
import org.lwjgl.openvr.VRVulkanTextureData
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAddress
import org.lwjgl.system.MemoryUtil.memUTF8
import org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT
import org.lwjgl.vulkan.VkPhysicalDevice
import java.lang.RuntimeException

fun checkAndInitVrSupport(appState: ApplicationState) {
    if (VR_IsRuntimeInstalled() && VR_IsHmdPresent()) {
        appState.useVR = true
        stackPush().use { stack ->
            val pInitError = stack.callocInt(1)
            val token = VR_InitInternal(pInitError, EVRApplicationType_VRApplication_Scene)
            assertVrSuccess(pInitError[0], "VR_InitInternal")
            OpenVR.create(token)
        }
        println("Using VR")
    } else {
        appState.useVR = false
    }
}

private fun assertVrSuccess(code: Int, functionName: String) {
    if (code == EVRCompositorError_VRCompositorError_DoNotHaveFocus) {
        println("No focus (${System.currentTimeMillis() / 1000})")
    } else if (code != 0) {
        throw RuntimeException("$functionName returned $code")
    }
}

fun addVrInstanceExtensions(extensions: MutableSet<String>) {
    val extensionStringSize = VRCompositor_GetVulkanInstanceExtensionsRequired(null)
    if (extensionStringSize > 0) {
        val extensionString = stackPush().use { stack ->
            val extensionBuffer = stack.calloc(extensionStringSize)
            VRCompositor_GetVulkanInstanceExtensionsRequired(extensionBuffer)
            memUTF8(memAddress(extensionBuffer))
        }

        val extensionArray = extensionString.split(" ")
        println("The following Vulkan instance extensions are required for OpenVR:")
        for (extension in extensionArray) {
            println(extension)
        }
        println()
        extensions.addAll(extensionArray)
    }
}

fun addVrDeviceExtensions(device: VkPhysicalDevice, extensions: MutableSet<String>) {
    val extensionStringSize = VRCompositor_GetVulkanDeviceExtensionsRequired(device.address(), null)
    if (extensionStringSize > 0) {
        val extensionString = stackPush().use { stack ->
            val extensionBuffer = stack.calloc(extensionStringSize)
            VRCompositor_GetVulkanDeviceExtensionsRequired(device.address(), extensionBuffer)
            memUTF8(memAddress(extensionBuffer))
        }

        val extensionArray = extensionString.split(" ")
        println("The following Vulkan device extensions are required for OpenVR:")
        for (extension in extensionArray) {
            println(extension)
        }
        println()
        extensions.addAll(extensionArray)
    }
}

fun getVrPoses(appState: ApplicationState) {
    stackPush().use { stack ->
        val renderPoses = TrackedDevicePose.callocStack(k_unMaxTrackedDeviceCount, stack)
        val gamePoses = null
        assertVrSuccess(VRCompositor_WaitGetPoses(renderPoses, gamePoses), "WaitGetPoses")

        val renderPose = renderPoses[0]
        if (renderPose.bPoseIsValid()) {
            println("velocity x is ${renderPose.vVelocity().v(0)}")
        } else {
            println("Render pose invalid")
        }
    }
}

fun submitSwapchainImageToVr(appState: ApplicationState, swapchainImage: SwapchainImage) {
    stackPush().use { stack ->
        val vulkanTexture = VRVulkanTextureData.callocStack(stack)
        vulkanTexture.m_nImage(swapchainImage.image)
        vulkanTexture.m_pDevice(appState.device.address())
        vulkanTexture.m_pPhysicalDevice(appState.physicalDevice.address())
        vulkanTexture.m_pInstance(appState.instance.address())
        vulkanTexture.m_pQueue(appState.graphicsQueue.address())
        vulkanTexture.m_nQueueFamilyIndex(appState.queueFamilyIndex!!)
        vulkanTexture.m_nWidth(appState.swapchainWidth!!)
        vulkanTexture.m_nHeight(appState.swapchainHeight!!)
        vulkanTexture.m_nFormat(appState.swapchainColorFormat!!)
        vulkanTexture.m_nSampleCount(VK_SAMPLE_COUNT_1_BIT)

        val submitTexture = Texture.callocStack(stack)
        submitTexture.handle(vulkanTexture.address())
        submitTexture.eType(ETextureType_TextureType_Vulkan)
        submitTexture.eColorSpace(EColorSpace_ColorSpace_Auto)

        assertVrSuccess(
            VRCompositor_Submit(EVREye_Eye_Left, submitTexture, null, EVRSubmitFlags_Submit_Default),
            "VRCompositor_Submit"
        )
        assertVrSuccess(
            VRCompositor_Submit(EVREye_Eye_Right, submitTexture, null, EVRSubmitFlags_Submit_Default),
            "VRCompositor_Submit"
        )
    }
}

fun destroyVrIfNeeded(appState: ApplicationState) {
    if (appState.useVR!!) {
        VR_ShutdownInternal()
    }
}
