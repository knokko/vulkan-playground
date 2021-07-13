package playground.vertex

import org.lwjgl.system.MemoryUtil.*
import java.nio.ByteBuffer

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

        fun createArray(buffer: ByteBuffer, position: Long, length: Long): Array<BasicVertex> {
            if (position < 0) throw IllegalArgumentException("position ($position) < 0")
            val endIndex = position + length * SIZE
            if (length > 0 && endIndex >= buffer.capacity())
                throw IllegalArgumentException("Creating this array would allow buffer overflow")
            if (endIndex < position) throw IllegalArgumentException("Size computation caused integer overflow")

            val startAddress = memAddress(buffer) + position
            return Array(length.toInt()) { index -> BasicVertex(startAddress + index * SIZE) }
        }
    }
}
