package playground

import org.lwjgl.system.MemoryUtil.memAlloc
import java.lang.RuntimeException
import java.nio.ByteBuffer

fun assertSuccess(returnCode: Int, functionName: String, functionContext: String?) {
    if (returnCode < 0) {
        if (functionContext != null) {
            throw VulkanException("$functionName ($functionContext) returned $returnCode")
        } else {
            throw VulkanException("$functionName returned $returnCode")
        }
    }
}

fun assertSuccess(returnCode: Int, functionName: String) {
    assertSuccess(returnCode, functionName, null)
}

class VulkanException(message: String): RuntimeException(message)

fun mallocBundledResource(path: String): ByteBuffer {
    val stream = ApplicationState::class.java.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Can't load resource $path")

    val array = stream.readBytes()
    stream.close()
    val buffer = memAlloc(array.size)
    buffer.put(array, 0, array.size)

    return buffer
}