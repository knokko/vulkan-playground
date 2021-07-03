package playground.vertex

import org.lwjgl.system.MemoryUtil.memGetInt
import org.lwjgl.system.MemoryUtil.memPutInt

@JvmInline
value class BasicVertex(val address: Long) {

    val position: Vec3f
    get() = Vec3f(address + OFFSET_BASE_POSITION)

    val normal: Vec3f
    get() = Vec3f(address + OFFSET_BASE_NORMAL)

    val textureCoordinates: Vec2f
    get() = Vec2f(address + OFFSET_TEXTURE_COORDINATES)

    var matrixIndex: Int
    get() = memGetInt(address + OFFSET_MATRIX_INDEX)
    set(value) = memPutInt(address + OFFSET_MATRIX_INDEX, value)

    companion object {

        const val OFFSET_BASE_POSITION = 0
        const val OFFSET_BASE_NORMAL = OFFSET_BASE_POSITION + Vec3f.SIZE
        const val OFFSET_TEXTURE_COORDINATES = OFFSET_BASE_NORMAL + Vec3f.SIZE
        const val OFFSET_MATRIX_INDEX = OFFSET_TEXTURE_COORDINATES + Vec2f.SIZE

        const val SIZE = OFFSET_MATRIX_INDEX + Int.SIZE_BYTES
    }
}
