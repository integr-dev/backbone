package net.integr.backbone.systems.storage

import net.integr.backbone.systems.storage.config.ConfigHandler
import net.integr.backbone.systems.storage.database.DatabaseConnection
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.reflect.typeOf

class ResourcePool(origin: Path, id: String) {
    val location: Path = origin.resolve(id)

    fun allocate(id: String): ResourceLocation {
        return ResourceLocation(this, id)
    }

    fun create() {
        location.createDirectories()
    }

    fun exists(): Boolean {
        return location.toFile().exists()
    }

    fun database(id: String): DatabaseConnection {
        val location = allocate(id)
        location.create()
        return DatabaseConnection(location)
    }

    fun listFiles(): List<Path> {
        return location.toFile().listFiles()?.map { it.toPath() } ?: emptyList()
    }

    inline fun <reified T : Any> config(id: String): ConfigHandler<T> {
        val location = allocate(id)
        location.create()
        return ConfigHandler(location, typeOf<T>())
    }

    companion object {
        fun fromRoot(id: String): ResourcePool {
            return ResourcePool(Path.of("."), id)
        }

        fun fromStorage(id: String): ResourcePool {
            return ResourcePool(Path.of("./storage"), id)
        }

        fun fromConfig(id: String): ResourcePool {
            return ResourcePool(Path.of("./config"), id)
        }

        fun getScripts(): ResourcePool {
            return ResourcePool(Path.of("./"), "scripts")
        }
    }
}