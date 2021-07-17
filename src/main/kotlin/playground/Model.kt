package playground

import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand
import playground.vertex.BasicVertex
import java.nio.IntBuffer

/**
 * Represents a pair of a vertex buffer and index buffer, as well as the number of transformation matrices the model
 * uses.
 *
 * Every Model occupies a part of the vertex memory and a part of the index memory, but NOT a part of the storage
 * memory (but ModelInstance's do).
 *
 * Model's can't be drawn directly because they don't occupy any storage memory for its transformation matrices. Only
 * ModelInstance's can be drawn directly because they do occupy some storage memory. This system makes it possible to
 * have multiple ModelInstance's that have the same Model (and thus share vertex data and index data), but have their
 * own transformation matrices (so each ModelInstance can be drawn at a different position).
 */
class Model(val vertexOffset: Int, val numVertices: Int, val indexOffset: Int, val numIndices: Int, val numMatrices: Int)

/**
 * Represents a pair of a Model and an index into the buffer for transformation matrices. The matrices at index
 * `firstMatrixIndex` (inclusive) until `firstMatrixIndex + model.numMatrices` (exclusive) are reserved for this ModelInstance.
 */
class ModelInstance(val model: Model, val firstMatrixIndex: Int) {

    fun storeInDrawCommand(dest: VkDrawIndexedIndirectCommand) {
        dest.indexCount(model.numIndices)
        dest.instanceCount(1)
        dest.firstIndex(model.indexOffset)
        dest.vertexOffset(model.vertexOffset)
        dest.firstInstance(firstMatrixIndex)
    }
}

class ModelRequest(
    val numVertices: Int, val numIndices: Int, val numMatrices: Int,
    val fill: (vertices: Array<BasicVertex>, indices: IntBuffer) -> Unit,
    val store: (appState: ApplicationState, resultModel: Model) -> Unit
)
