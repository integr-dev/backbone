/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return ConfigHandler(location, T::class)
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