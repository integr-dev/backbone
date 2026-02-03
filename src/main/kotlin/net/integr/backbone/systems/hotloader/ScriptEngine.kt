package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.configuration.Script
import net.integr.backbone.systems.hotloader.configuration.UtilityScript
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import java.io.File
import kotlin.io.path.name
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object ScriptEngine {

    val logger = Backbone.LOGGER.derive("script-engine")

    var scripts = mutableMapOf<String, ScriptState>()

    // Create the host once to reuse its internal compiler warm-up
    private val scriptingHost = BasicJvmScriptingHost()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun loadScripts(): Boolean {
        val files = Backbone.SCRIPT_POOL
            .listFiles()
            .filter { it.name.endsWith(".bb.kts") }

        logger.info("Preparing ${files.size} scripts...")

        val jobs = mutableListOf<Job>()
        val newScripts = mutableMapOf<String, ScriptState>() // Prepare buffer for fast swap

        var errs = false

        for (file in files) {
            jobs += coroutineScope.launch {
                try {
                    val oldLifecycle = scripts[file.name] // Grab the old script
                    val script = compileScript(file.toFile()) // Compile the new script
                    logger.info("Compiled script: ${file.name}")

                    if (oldLifecycle != null) {
                        script.updateStatesFrom(oldLifecycle.lifecycle) // Load sustained states from odl into the new script state
                        logger.info("Transferred state on script: ${file.name}")
                    }

                    newScripts[file.name] = ScriptState(false, script) // Push the parse script into the buffer
                } catch (e: Exception) {
                    logger.severe("Failed to prepare script: ${file.name} (${e.javaClass.simpleName})")
                    errs = true
                }
            }
        }

        jobs.forEach { it.join() }

        // Once all scripts are compiled, we do the swap
        val unloadErrs = unloadScripts()
        errs = errs || unloadErrs

        logger.info("Enabling ${newScripts.size} scripts...")

        for ((name, state) in newScripts) {
            try {
                state.lifecycle.onLoad() // Enable the script
                state.enabled = true
                logger.info("Enabled script: $name")
            } catch (e: Exception) {
                logger.severe("Failed to enable script: $name (${e.javaClass.simpleName})")
                e.printStackTrace()
                errs = true
            }
        }

        scripts = newScripts // Swap

        logger.info("Loaded ${scripts.size} scripts.")

        return errs
    }

    fun unloadScripts(): Boolean {
        var errs = false

        for ((name, state) in scripts) {
            try {
                state.lifecycle.onUnload()
                state.enabled = false
                logger.info("Disabled script: $name")
            } catch (e: Exception) {
                logger.severe("Failed to disable script: $name")
                e.printStackTrace()
                errs = true
            }
        }

        scripts.clear()
        return errs
    }

    fun compileScript(file: File): ManagedLifecycle {
        val compilationConfig = createJvmCompilationConfigurationFromTemplate<Script>()
        val evaluationConfig = createJvmEvaluationConfigurationFromTemplate<Script>()
        val result = scriptingHost.eval(file.toScriptSource(), compilationConfig, evaluationConfig)

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
            logger.warning("Script '$name' is already disabled.")
            throw IllegalArgumentException("Script '$name' is already disabled.")
        }

        try {
            script.onUnload()
            state.enabled = false
            logger.info("Disabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to disable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while disabling script.")
        }
    }

    fun enableScript(name: String) {
        val state = getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (state.enabled) {
            logger.warning("Script '$name' is already enabled.")
            throw IllegalArgumentException("Script '$name' is already enabled.")
        }

        try {
            script.onLoad()
            state.enabled = true
            logger.info("Enabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to enable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while enabling script.")
        }
    }

    class ScriptState(var enabled: Boolean, var lifecycle: ManagedLifecycle)
}