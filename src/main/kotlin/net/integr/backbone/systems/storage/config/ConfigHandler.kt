package net.integr.backbone.systems.storage.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.storage.ResourceLocation
import tools.jackson.dataformat.yaml.YAMLMapper
import kotlin.reflect.KClass


class ConfigHandler<T : Any>(private val file: ResourceLocation, private val klass: KClass<T>) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val logger = Backbone.LOGGER.derive("config-handler")

    private var cachedState: T? = null

    fun writeState(obj: T) {
        logger.info("Writing config state to ${file.location.absolutePath}")
        cachedState = obj

        coroutineScope.launch {
            val str = yaml.writeValueAsString(obj);
            file.location.writeText(str)
        }
    }

    fun writeStateSync(obj: T) {
        logger.info("Writing config state to ${file.location.absolutePath}")
        cachedState = obj

        val str = yaml.writeValueAsString(obj);
        file.location.writeText(str)
    }

    fun updateAndGetStateSync(): T {
        val str = file.location.readText()
        val state = yaml.readValue<T>(str, klass.java)
        cachedState = state
        return state
    }

    fun updateSync() {
        val str = file.location.readText()
        val state = yaml.readValue<T>(str, klass.java)
        cachedState = state
    }

    fun update() {
        coroutineScope.launch {
            val str = file.location.readText()
            val state = yaml.readValue<T>(str, klass.java)
            cachedState = state
        }
    }

    fun getState() = cachedState

    companion object {
        val yaml: YAMLMapper = YAMLMapper
            .builder()
            .findAndAddModules()
            .build()
    }
}
