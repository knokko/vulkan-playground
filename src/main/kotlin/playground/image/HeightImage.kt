package playground.image

class HeightImage(val width: Int, val height: Int) {

    private val values = FloatArray(width * height)

    fun getValueAt(x: Int, y: Int): Float {
        return values[x + y * width]
    }

    fun setValueAt(x: Int, y: Int, newValue: Float) {
        values[x + y * width] = newValue
    }
}
