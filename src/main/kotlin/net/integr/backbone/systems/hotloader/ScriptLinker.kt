package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.ScriptEngine.unloadScripts
import net.integr.backbone.systems.hotloader.configuration.Script
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.name
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.with
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object ScriptLinker {
    private val logger = Backbone.LOGGER.derive("script-linker")

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun compileAndLink(): Boolean {
        var errs = false

        val utilityScripts = Backbone.SCRIPT_POOL
            .listFiles()
            .filter { it.name.endsWith(".bbu.kts") }

        val scripts = Backbone.SCRIPT_POOL
            .listFiles()
            .filter { it.name.endsWith(".bb.kts") }


        logger.info("Processing ${utilityScripts.size} utility scripts...")
        logger.info("Processing ${scripts.size} scripts...")

        val jobs = mutableListOf<Job>()
        val classLoaders = mutableListOf<ClassLoader>()

        logger.info("Now compiling utility scripts...")

        for (file in utilityScripts) {
            jobs += coroutineScope.launch {
                try {
                    logger.info("[${file.name}] Compiling...")
                    val classLoader = ScriptCompiler.compileUtilityScript(file.toFile())
                    logger.info("[${file.name}] Done compiling: $classLoader")

                    if (classLoader != null) {
                        classLoaders += classLoader
                    } else {
                        logger.warning("[${file.name}] No ClassLoader found.")
                    }
                } catch (e: Exception) {
                    logger.severe("[${file.name}] Failed to compile. (${e.javaClass.simpleName})")
                    errs = true
                }
            }
        }

        jobs.forEach { it.join() }
        jobs.clear()

        logger.info("Resolved ${classLoaders.size} utility script classloaders.")
        logger.info("Now generating temp artifacts and full ClassLoader...")

        val fullClassLoader = ExtendableClassLoader(Backbone::class.java.classLoader)
        val jarFiles = mutableListOf<File>()

        for (loader in classLoaders) {
            val entries = ScriptCompiler.getClassloaderEntries(loader)
            val tempJar = ScriptCompiler.createTempJar(entries)

            fullClassLoader.addClasses(entries)
            jarFiles += tempJar
        }

        logger.info("Now compiling scripts...")

        val newScripts = ConcurrentHashMap<String, ScriptStore.State>()

        val compilationConfig = createJvmCompilationConfigurationFromTemplate<Script>().with {
            hostConfiguration(ScriptingHostConfiguration {
                jvm {
                    baseClassLoader(fullClassLoader)
                }
            })

            updateClasspath(jarFiles)
        } //TODO: Deps from ivy are missing during runtime

        val evaluationConfig = createJvmEvaluationConfigurationFromTemplate<Script>().with {
            jvm {
                baseClassLoader(fullClassLoader)
            }
        }

        for (file in scripts) {
            jobs += coroutineScope.launch {
                try {
                    val oldLifecycle = ScriptStore.scripts[file.name]

                    logger.info("[${file.name}] Compiling...")

                    val lifecycle = ScriptCompiler.compileScript(
                        file = file.toFile(),
                        compilationConfiguration = compilationConfig,
                        evaluationConfiguration = evaluationConfig)

                    logger.info("[${file.name}] Done compiling.")

                    if (oldLifecycle != null) {
                        lifecycle.updateStatesFrom(oldLifecycle.lifecycle)
                        logger.info("[${file.name}] Transferred state from old script")
                    }

                    newScripts[file.name] = ScriptStore.State(false, lifecycle)

                } catch (e: Exception) {
                    logger.severe("[${file.name}] Failed to compile. (${e.javaClass.simpleName})")
                    e.printStackTrace()
                    errs = true
                }
            }
        }

        jobs.forEach { it.join() }

        logger.info("Compiled ${newScripts.size} scripts.")
        logger.info("Now swapping hot...")

        errs = errs || unloadScripts()
        logger.info("Unloaded old scripts.")

        logger.info("Enabling ${newScripts.size} scripts...")

        for ((name, state) in newScripts) {
            try {
                state.lifecycle.onLoad()
                state.enabled = true
                logger.info("[$name] Enabled script")
            } catch (e: Exception) {
                logger.severe("[$name] Failed to enable. (${e.javaClass.simpleName})")
                e.printStackTrace()
                errs = true
            }
        }

        ScriptStore.scripts = newScripts
        logger.info("Loaded and swapped ${ScriptStore.scripts.size} scripts.")

        return errs
    }
}