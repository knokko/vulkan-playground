package playground

import java.lang.RuntimeException

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