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

package net.integr.backbone.systems.persistence.config
import tools.jackson.module.kotlin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.logger.BackboneLogger
import net.integr.backbone.systems.persistence.ResourceLocation
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import kotlin.reflect.KClass

/**
 * Handles the serialization and deserialization of configuration files using an object mapper.
 *
 * @param file The `ResourceLocation` representing the configuration file.
 * @param klass The Kotlin class representing the structure of the configuration.
 * @param mapper The `ObjectMapper` used for serialization and deserialization.
 *
 * @since 1.0.0
 */
class ConfigHandler<T : Any>(private val file: ResourceLocation, private val klass: KClass<T>, private val mapper: ObjectMapper, private val default: T? = null) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val logger = BackboneLogger("backbone.config-handler", true, Backbone.pluginInternal) // Trickery here to avoid circular dependency

    private var cachedState: T? = null

    /**
     * Store the current state of the config to the file.
     *
     * @param obj The object representing the new state of the config.
     *
     * @since 1.0.0
     */
    fun writeState(obj: T) {
        cachedState = obj

        coroutineScope.launch {
            writeStateSync(obj)
        }
    }

    /**
     * Synchronously store the current state of the config to the file.
     *
     * @param obj The object representing the new state of the config.
     *
     * @since 1.0.0
     */
    fun writeStateSync(obj: T) {
        cachedState = obj

        try {
            val str = mapper.writeValueAsString(obj)
            file.location.writeText(str)
        } catch (e: Exception) {
            logger.severe("Failed to write config file ${file.location}: ${e.message}")
        }
    }

    /**
     * Synchronously rewrite the current state of the config to the file.
     * This will use the cached state, if there is no cached state, this will do nothing.
     *
     * @since 1.8.0
     */
    fun rewriteStateSync() {
        cachedState?.let { writeStateSync(it) }
    }

    /**
     * Synchronously update and get the current state of the config from the file.
     *
     * @return The current state of the config. Returns null if there was an error reading the file or parsing the config.
     *
     * @since 1.0.0
     */
    fun updateAndGetStateSync(): T? {
        try {
            val str = file.location.readText()
            val state = mapper.readValue(str, klass.java)
            cachedState = state
            return state
        } catch (e: Exception) {
            logger.severe("Failed to read config file ${file.location}: ${e.message}")
            if (default != null) writeStateSync(default)
            return default
        }
    }

    /**
     * Synchronously update the current state of the config from the file.
     *
     * @since 1.0.0
     */
    fun updateSync() {
        try {
            val str = file.location.readText()
            val state = mapper.readValue(str, klass.java)
            cachedState = state
        } catch (e: Exception) {
            logger.severe("Failed to read config file ${file.location}: ${e.message}")
            if (default != null) writeStateSync(default)
        }
    }

    /**
     * Update the current state of the config from the file.
     *
     * @since 1.0.0
     */
    fun update() {
        coroutineScope.launch {
            updateSync()
        }
    }

    /**
     * Get the current state of the config.
     * **Important:** This will not read a file, if you need state from file, use
     * [updateAndGetStateSync]
     *
     * @return The current state of the config.
     *
     * @since 1.0.0

     */
    fun getState() = cachedState

    companion object {
        val YAML: YAMLMapper = YAMLMapper
            .builder()
            .addModule(KotlinModule.Builder().build())
            .findAndAddModules()
            .build()

        val JSON: JsonMapper = JsonMapper
            .builder()
            .addModule(KotlinModule.Builder().build())
            .findAndAddModules()
            .build()
    }
}
