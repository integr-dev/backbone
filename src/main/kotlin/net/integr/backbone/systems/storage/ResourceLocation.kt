package net.integr.backbone.systems.storage

import java.io.File

class ResourceLocation(val pool: ResourcePool, id: String) {
    val location: File = pool.location.resolve(id).toFile()

    fun create(): Boolean {
        pool.create()
        return location.createNewFile()
    }

    fun exists(): Boolean {
        return location.exists()
    }
}