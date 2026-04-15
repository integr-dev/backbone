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

package net.integr.backbone.systems.persistence

import net.integr.backbone.systems.persistence.config.ConfigHandler
import net.integr.backbone.systems.persistence.database.DatabaseConnection
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * Represents a pool of resources, providing methods to allocate and manage files, databases, and configurations.
 *
 * @param origin The base path for this resource pool.
 * @param id The identifier for this resource pool, which will be a subdirectory within the origin.
 *
 * @since 1.0.0
 */
class ResourcePool(val origin: Path, id: String) {
    /**
     * The backing path for this resource pool.
     *
     * @since 1.0.0
     */
    val location: Path = origin.resolve(id)

    /**
     * Allocate a new resource location within this pool.
     *
     * @param id The identifier for the new resource location.
     * @return A `ResourceLocation` object representing the new resource.
     *
     * @since 1.0.0
     */
    fun allocate(id: String): ResourceLocation {
        return ResourceLocation(this, id)
    }

    /**
     * Allocates a new file within this resource pool, ensuring its creation.
     *
     * @param id The identifier for the new file.
     * @return A `ResourceLocation` object representing the new file.
     *
     * @since 1.0.0
     */
    fun file(id: String): ResourceLocation {
        val location = allocate(id)
        location.create()
        return location
    }

    /**
     * Create the directory for this resource pool if it doesn't already exist.
     *
     * @since 1.0.0
     */
    fun create() {
        location.createDirectories()
    }

    /**
     * Checks if the directory for this resource pool exists.
     *
     * @return `true` if the directory exists, `false` otherwise.
     *
     * @since 1.0.0
     */
    fun exists(): Boolean {
        return location.toFile().exists()
    }

    /**
     * Allocates a new database within this resource pool, ensuring its creation.
     *
     * @param id The identifier for the new database.
     * @return A `DatabaseConnection` object representing the new database.
     *
     * @since 1.0.0
     */
    fun database(id: String): DatabaseConnection {
        val location = allocate(id)
        location.create()
        return DatabaseConnection(location)
    }

    /**
     * Lists all files and directories directly within this resource pool.
     *
     * @return A list of `Path` objects representing the files and directories.
     *
     * @since 1.0.0
     */
    fun listFiles(): List<Path> {
        return location.toFile().listFiles()?.map { it.toPath() } ?: emptyList()
    }

    /**
     * Allocates a new configuration file within this resource pool, ensuring its creation.
     *
     * @param id The identifier for the new configuration file.
     * @return A `ConfigHandler` object representing the new configuration.
     *
     * @since 1.0.0
     */
    inline fun <reified T : Any> config(id: String): ConfigHandler<T> {
        val location = allocate(id)
        location.create()
        val handler = ConfigHandler(location, T::class, ConfigHandler.YAML)
        handler.updateSync()
        handler.rewriteStateSync()
        return handler
    }

    /**
     * Allocates a new configuration file within this resource pool, ensuring its creation.
     * This method also writes the provided initial state to the configuration file.
     *
     * @param id The identifier for the new configuration file.
     * @param default The initial state to write to the configuration file.
     * @return A `ConfigHandler` object representing the new configuration.
     *
     * @since 1.8.0
     */
    inline fun <reified T : Any> config(id: String, default: T): ConfigHandler<T> {
        val location = allocate(id)
        location.create()
        val handler = ConfigHandler(location, T::class, ConfigHandler.YAML, default)
        handler.updateSync()
        handler.rewriteStateSync()
        return handler
    }

    /**
     * Allocates a new configuration file within this resource pool, ensuring its creation.
     * This method uses JSON for serialization instead of YAML.
     *
     * @param id The identifier for the new configuration file.
     * @return A `ConfigHandler` object representing the new configuration.
     *
     * @since 1.7.0
     */
    inline fun <reified T : Any> configJson(id: String): ConfigHandler<T> {
        val location = allocate(id)
        location.create()
        val handler = ConfigHandler(location, T::class, ConfigHandler.YAML)
        handler.updateSync()
        handler.rewriteStateSync()
        return handler
    }

    /**
     * Allocates a new configuration file within this resource pool, ensuring its creation.
     * This method uses JSON for serialization instead of YAML and also writes the provided initial state to the configuration file.
     *
     * @param id The identifier for the new configuration file.
     * @param default The initial state to write to the configuration file.
     * @return A `ConfigHandler` object representing the new configuration.
     *
     * @since 1.8.0
     */
    inline fun <reified T : Any> configJson(id: String, default: T): ConfigHandler<T> {
        val location = allocate(id)
        location.create()
        val handler = ConfigHandler(location, T::class, ConfigHandler.YAML, default)
        handler.updateSync()
        handler.rewriteStateSync()
        return handler
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourcePool

        return location == other.location
    }

    override fun hashCode(): Int {
        return location.hashCode()
    }

    companion object {
        /**
         * Creates a resource pool from the root directory of the server.
         *
         * @param id The identifier for the resource pool.
         * @return A `ResourcePool` object.
         *
         * @since 1.0.0
         */
        fun fromRoot(id: String): ResourcePool {
            return ResourcePool(Path.of("."), id)
        }

        /**
         * Creates a resource pool from the storage directory of the server.
         *
         * @param id The identifier for the resource pool.
         * @return A `ResourcePool` object.
         *
         * @since 1.0.0
         */
        fun fromStorage(id: String): ResourcePool {
            return ResourcePool(Path.of("./storage"), id)
        }

        /**
         * Creates a resource pool from the config directory of the server.
         *
         * @param id The identifier for the resource pool.
         * @return A `ResourcePool` object.
         *
         * @since 1.0.0
         */
        fun fromConfig(id: String): ResourcePool {
            return ResourcePool(Path.of("./config"), id)
        }

        /**
         * Creates a resource pool from the scripts directory of the server.
         *
         * @return A `ResourcePool` object.
         *
         * @since 1.0.0
         */
        @ApiStatus.Internal
        fun getScripts(): ResourcePool {
            return ResourcePool(Path.of("./"), "scripts")
        }
    }
}