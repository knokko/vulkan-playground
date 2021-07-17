package playground

fun requestTerrainModel(
                       /**
                        * The returned Model will consist of vertexFrequency * vertexFrequency vertices
                        */
                       vertexFrequency: Int,
                       store: (appState: ApplicationState, resultModel: Model) -> Unit
): ModelRequest {

    if (vertexFrequency < 2) {
        throw IllegalArgumentException("vertexFrequency ($vertexFrequency) must be at least 0")
    }

    // We get overflow hazards if we go much further than 5000 vertices. Besides, this would drain memory...
    if (vertexFrequency > 5000) {
        throw IllegalArgumentException("vertexFrequency ($vertexFrequency) can be at most 5000")
    }

    val numVertices = vertexFrequency * vertexFrequency
    val numIndices = 6 * (vertexFrequency - 1) * (vertexFrequency - 1)

    return ModelRequest(numVertices, numIndices, 1, { vertices, indices ->
        for (indexZ in 0 until vertexFrequency) {
            for (indexX in 0 until vertexFrequency) {
                val vertexIndex = indexX + vertexFrequency * indexZ
                val x = indexX.toFloat() / (vertexFrequency - 1).toFloat()
                val z = indexZ.toFloat() / (vertexFrequency - 1).toFloat()

                val vertex = vertices[vertexIndex]
                vertex.position.x = x
                vertex.position.y = 0f
                vertex.position.z = z
                vertex.normal.x = 0f
                vertex.normal.y = 1f
                vertex.normal.z = 0f
                vertex.textureCoordinates.x = x
                vertex.textureCoordinates.y = z
                vertex.matrixIndex = 0
            }
        }

        for (lowZ in 0 until vertexFrequency - 1) {
            val highZ = lowZ + 1
            for (lowX in 0 until vertexFrequency - 1) {
                val highX = lowX + 1

                val indexLL = lowX + vertexFrequency * lowZ
                val indexHL = highX + vertexFrequency * lowZ
                val indexLH = lowX + vertexFrequency * highZ
                val indexHH = highX + vertexFrequency * highZ

                indices.put(indexLH)
                indices.put(indexHH)
                indices.put(indexHL)

                indices.put(indexHL)
                indices.put(indexLL)
                indices.put(indexLH)
            }
        }
    }, store)
}
