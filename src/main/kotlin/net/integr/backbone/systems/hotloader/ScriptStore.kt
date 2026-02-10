package net.integr.backbone.systems.hotloader

import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import java.util.concurrent.ConcurrentHashMap

object ScriptStore {
    class State(var enabled: Boolean, var lifecycle: ManagedLifecycle)

    var scripts = ConcurrentHashMap<String, State>()

    fun getScriptNames() = scripts.map { it.key.substringBefore(".bb.kts") }

    fun getScriptByName(name: String): State? {
        val key = scripts.keys.firstOrNull { it.substringBefore(".bb.kts") == name }
        return scripts[key]
    }

    fun getEnabledScripts(): List<String> {
        return scripts.filter { it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

    fun getDisabledScripts(): List<String> {
        return scripts.filter { !it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

}