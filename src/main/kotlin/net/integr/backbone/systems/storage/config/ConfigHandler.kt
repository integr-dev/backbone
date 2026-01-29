package net.integr.backbone.systems.storage.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.integr.backbone.systems.storage.ResourceLocation
import kotlin.reflect.KType

class ConfigHandler<T : Any>(private val file: ResourceLocation, private val klass: KType) {
    @Suppress("UNCHECKED_CAST")
    private fun serializer(): KSerializer<T> {
        return Yaml.default.serializersModule.serializer(klass) as KSerializer<T>
    }

    fun writeState(obj: T) {
        val str = Yaml.default.encodeToString(serializer(), obj)
        file.location.writeText(str)
    }

    fun readState(): T {
        val str = file.location.readText()
        return Yaml.default.decodeFromString(serializer(), str)
    }
}
