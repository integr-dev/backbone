package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.annotations.CompilerOptions
import net.integr.backbone.systems.hotloader.configuration.ImportHandler
import net.integr.backbone.systems.hotloader.configuration.Script
import net.integr.backbone.systems.hotloader.configuration.ScriptClassLoader
import net.integr.backbone.systems.hotloader.configuration.ScriptConfiguration
import net.integr.backbone.systems.hotloader.configuration.UtilityScript
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.io.path.name
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.api.with
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.impl.getOrCreateActualClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
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

        val utilFiles = Backbone.SCRIPT_POOL
            .listFiles()
            .filter { it.name.endsWith(".bbu.kts") }

        logger.info("Preparing ${files.size} scripts...")
        logger.info("Preparing ${utilFiles.size} utility scripts...")

        val jobs = mutableListOf<Job>()
        val newScripts = mutableMapOf<String, ScriptState>() // Prepare buffer for fast swap

        var errs = false

        val jarFiles = mutableListOf<File>()
        val cldEntries = mutableMapOf<String, ByteArray>()

        for (file in utilFiles) {
            jobs += coroutineScope.launch {
                try {
                    val new = compileUtilityScript(file.toFile()) // Compile the utility script
                    logger.info("Compiled utility script: ${file.name}")

                    if (new != null) {
                        jarFiles.add(new.second)
                        cldEntries.putAll(new.first)
                    }
                } catch (e: Exception) {
                    logger.severe("Failed to compile utility script: ${file.name} (${e.javaClass.simpleName})")
                    errs = true
                }
            }
        }

        jobs.forEach { it.join() }
        jobs.clear()

        val scriptClassLoader = ScriptClassLoader(Backbone::class.java.classLoader)
        scriptClassLoader.addClasses(cldEntries)

        // test
        val a = scriptClassLoader.findResource("com.github.javafaker.Faker")
        println("RESULT: " + a)

        val compilationConfig = createJvmCompilationConfigurationFromTemplate<Script>().with {
            hostConfiguration(ScriptingHostConfiguration {
                jvm {
                    baseClassLoader(scriptClassLoader)
                }
            })

            jvm {
                defaultImports(DependsOn::class, Repository::class, CompilerOptions::class)
                dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
                this.updateClasspath(jarFiles)
            }
        }

        val evaluationConfig = createJvmEvaluationConfigurationFromTemplate<Script>().with {
            jvm {
                baseClassLoader(scriptClassLoader)
            }
        }

        for (file in files) {
            jobs += coroutineScope.launch {
                try {
                    val oldLifecycle = scripts[file.name] // Grab the old script
                    val script = compileScript(file.toFile(), compilationConfig, evaluationConfig) // Compile the new script
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

    fun compileScript(file: File, compilationConfig: ScriptCompilationConfiguration, evaluationConfig: ScriptEvaluationConfiguration): ManagedLifecycle {
        val result = scriptingHost.eval(file.toScriptSource(), compilationConfig, evaluationConfig)

        result.reports.forEach { report ->
            if (report.severity >= ScriptDiagnostic.Severity.WARNING) {
                logger.warning("[${file.name}] [${report.severity}] ${report.message} (${report.location})")
                if (report.exception != null) report.exception?.printStackTrace()
            }
        }

        val evalValue = result.valueOrNull()?.returnValue
        if (evalValue is ResultValue.Value && evalValue.value is ManagedLifecycle) {
            return evalValue.value as ManagedLifecycle
        } else {
            throw IllegalStateException("Script did not return a ManagedLifecycle object. Found: $evalValue")
        }
    }

    fun compileUtilityScript(file: File): Pair<Map<String, ByteArray>, File>? {
        val compilationConfig = createJvmCompilationConfigurationFromTemplate<UtilityScript>()
        val evaluationConfig = createJvmEvaluationConfigurationFromTemplate<UtilityScript>()

        return runBlocking {
            val result = scriptingHost.compiler.invoke(file.toScriptSource(), compilationConfig)

            result.reports.forEach { report ->
                if (report.severity >= ScriptDiagnostic.Severity.WARNING) {
                    logger.warning("[${file.name}] [${report.severity}] ${report.message} (${report.location})")
                    if (report.exception != null) report.exception?.printStackTrace()
                }

                if (report.severity == ScriptDiagnostic.Severity.ERROR) {
                    throw IllegalStateException("FATAL: ${report.message}")
                }
            }

            val value = result.valueOrNull()
            if (value is KJvmCompiledScript) {
                val classLoader = value.getOrCreateActualClassloader(evaluationConfig)
                logger.info("Using classloader: ${classLoader.javaClass.name}")
                val entries = getEntriesFromScriptLoader(classLoader).toMutableMap()
                val jar = createTempJarFromScriptLoader(classLoader)
                logger.info("Added in-memory classes to compiler")

                val ivyResult = value.compilationConfiguration[ScriptCompilationConfiguration.dependencies]
                    ?.flatMap { (it as? JvmDependency)?.classpath ?: emptyList() }
                    ?: emptyList()

                for (f in ivyResult) {
                    entries[f.name] = f.readBytes()
                }

                return@runBlocking entries to jar
            } else return@runBlocking null
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

    fun createTempJarFromScriptLoader(loader: ClassLoader): File {
        val tempJar = File.createTempFile("script-deps-", ".jar")

        JarOutputStream(FileOutputStream(tempJar)).use { jos ->
            val entriesField = loader.javaClass.getDeclaredField("entries").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            val entries = entriesField.get(loader) as Map<String, ByteArray>

            entries.forEach { (path, bytes) ->
                jos.putNextEntry(JarEntry(path))
                jos.write(bytes)
                jos.closeEntry()
            }
        }

        return tempJar
    }

    fun getEntriesFromScriptLoader(loader: ClassLoader): Map<String, ByteArray> {
        val entriesField = loader.javaClass.getDeclaredField("entries").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        return entriesField.get(loader) as Map<String, ByteArray>
    }

    class ScriptState(var enabled: Boolean, var lifecycle: ManagedLifecycle)
}