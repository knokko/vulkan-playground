package playground

const val TRY_DEBUG = true

fun main() {
    val instanceManager = InstanceManager(TRY_DEBUG)
    instanceManager.run()
    instanceManager.destroy()
}
