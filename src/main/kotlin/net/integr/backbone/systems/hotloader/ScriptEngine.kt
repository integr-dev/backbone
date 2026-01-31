package net.integr.backbone.systems.hotloader

import net.integr.backbone.Backbone
import java.io.File
import kotlin.io.path.name
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

object ScriptEngine {
    class ScriptState(var enabled: Boolean, var lifecycle: ManagedLifecycle)

    val logger = Backbone.LOGGER.derive("script-engine")
    val scripts = mutableMapOf<String, ScriptState>()

    // Create the host once to reuse its internal compiler warm-up
    private val scriptingHost = BasicJvmScriptingHost()

    fun loadScripts(): Boolean {
        unloadScripts()
        val files = Backbone.SCRIPT_POOL.listFiles()

        var errs = false

        for (file in files) {
            try {
                val script = loadScript(file.toFile())
                script.onLoad()

                scripts[file.name] = ScriptState(true, script)
                logger.info("Loaded script: $file")
            } catch (e: Exception) {
                logger.severe("Failed to load script: $file")
                e.printStackTrace()
                errs = true
            }
        }

        return errs
    }

    fun getScriptNames() = scripts.map { it.key.substringBefore(".bb.kts") }
    fun getScriptByName(name: String): ScriptState? {
        val key = scripts.keys.firstOrNull { it.substringBefore(".bb.kts") == name }
        return scripts[key]
    }

    fun getEnabledScripts(): List<String> {
        return scripts.filter { it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

    fun getDisabledScripts(): List<String> {
        return scripts.filter { !it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

    fun disableScript(name: String) {
        val state = getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (!state.enabled) {
            logger.warning("Script $name is already disabled.")
            throw IllegalArgumentException("Script $name is already disabled.")
        }

        try {
            script.onUnload()
            state.enabled = false
            logger.info("Disabled script: $name")
        } catch (e: Exception) {
            logger.severe("Failed to disable script: $name")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while disabling script.")
        }
    }

    fun enableScript(name: String) {
        val state = getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (state.enabled) {
            logger.warning("Script $name is already enabled.")
            throw IllegalArgumentException("Script $name is already enabled.")
        }

        try {
            script.onLoad()
            state.enabled = true
            logger.info("Enabled script: $name")
        } catch (e: Exception) {
            logger.severe("Failed to enable script: $name")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while enabling script.")
        }
    }

    fun unloadScripts() {
        for ((name, script) in scripts) {
            try {
                script.lifecycle.onUnload()
                logger.info("Disabled script: $name")
            } catch (e: Exception) {
                logger.severe("Failed to disable script: $name")
                e.printStackTrace()
            }
        }

        scripts.clear()
    }

    fun loadScript(file: File): ManagedLifecycle {
        // 1. Setup Compilation: Inherit classpath and define 'plugin' variable
        val compilationConfig = createJvmCompilationConfigurationFromTemplate<ManagedLifecycle>()
        val evaluationConfig = ScriptEvaluationConfiguration {}

        // 2. Execute
        val result = scriptingHost.eval(file.toScriptSource(), compilationConfig, evaluationConfig)

        // 3. Process Results
        result.reports.forEach { report ->
            if (report.severity >= ScriptDiagnostic.Severity.WARNING) {
                logger.warning("[${report.severity}] ${report.message} (${report.location})")
            }
        }

        val evalValue = result.valueOrNull()?.returnValue
        if (evalValue is ResultValue.Value && evalValue.value is ManagedLifecycle) {
            return evalValue.value as ManagedLifecycle
        } else {
            throw IllegalStateException("Script did not return a ManagedLifecycle object. Found: $evalValue")
        }
    }
}