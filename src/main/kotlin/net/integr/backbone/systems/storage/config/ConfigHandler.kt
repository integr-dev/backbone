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

package net.integr.backbone.systems.storage.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.storage.ResourceLocation
import tools.jackson.core.PrettyPrinter
import tools.jackson.core.util.DefaultPrettyPrinter
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
class ConfigHandler<T : Any>(private val file: ResourceLocation, private val klass: KClass<T>, private val mapper: ObjectMapper) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val logger = Backbone.LOGGER.derive("config-handler")

    private var cachedState: T? = null

    /**
     * Store the current state of the config to the file.
     *
     * @param obj The object representing the new state of the config.
     *
     * @since 1.0.0
     */
    fun writeState(obj: T) {
        logger.info("Writing config state to ${file.location.absolutePath}")
        cachedState = obj

        coroutineScope.launch {
            val str = mapper.writeValueAsString(obj)
            file.location.writeText(str)
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
        logger.info("Writing config state to ${file.location.absolutePath}")
        cachedState = obj

        val str = mapper.writeValueAsString(obj)
        file.location.writeText(str)
    }

    /**
     * Synchronously update and get the current state of the config from the file.
     *
     * @return The current state of the config.
     *
     * @since 1.0.0
     */
    fun updateAndGetStateSync(): T {
        val str = file.location.readText()
        val state = mapper.readValue(str, klass.java)
        cachedState = state
        return state
    }

    /**
     * Synchronously update the current state of the config from the file.
     *
     * @since 1.0.0
     */
    fun updateSync() {
        val str = file.location.readText()
        val state = mapper.readValue(str, klass.java)
        cachedState = state
    }

    /**
     * Update the current state of the config from the file.
     *
     * @since 1.0.0
     */
    fun update() {
        coroutineScope.launch {
            val str = file.location.readText()
            val state = mapper.readValue(str, klass.java)
            cachedState = state
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
            .findAndAddModules()
            .build()

        val JSON: JsonMapper = JsonMapper
            .builder()
            .findAndAddModules()
            .build()
    }
}
