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

package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.integr.backbone.Backbone
import net.integr.backbone.systems.diagnostic.ProbeHandler
import net.integr.backbone.systems.hotloader.ScriptEngine.unloadScripts
import net.integr.backbone.systems.hotloader.configuration.Script
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.io.path.name
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.with
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

/**
 * The central object for managing the compilation, linking, and hot-reloading of scripts.
 *
 * This object orchestrates the entire hot-reloading process, including:
 * - Discovering and compiling utility scripts (`.bbu.kts`) and regular scripts (`.bb.kts`).
 * - Creating a custom classloader hierarchy to isolate script dependencies and enable hot-swapping.
 * - Transferring sustained states between old and new script instances during reloads.
 * - Managing the lifecycle (loading, unloading, enabling) of scripts.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
@OptIn(ExperimentalAtomicApi::class)
object ScriptLinker {
    private val logger = ScriptEngine.logger.derive("linker")

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val reloadEpoch = AtomicLong(0)

    /**
     * Tracks old classloaders across reloads to allow explicit cleanup.
     * Without closing old classloaders, they accumulate in memory and cause classloader leaks.
     */
    private val oldClassLoaders = mutableListOf<ExtendableClassLoader>()

    /**
     * Compiles and links all scripts found in the [Backbone.SCRIPT_POOL] directory.
     *
     * This method performs the following steps:
     * 1. Discovers all utility scripts (`.bbu.kts`) and regular scripts (`.bb.kts`).
     * 2. Compiles utility scripts, creating a custom classloader and temporary JAR files for their classes and dependencies.
     * 3. Creates a unified `ExtendableClassLoader` that includes all utility script classes and their dependencies.
     * 4. Compiles regular scripts using the unified classloader.
     * 5. If a script was previously loaded, its sustained state is transferred to the new instance.
     * 6. Unloads all previously active scripts.
     * 7. Loads and enables all newly compiled scripts.
     *
     * Any errors encountered during compilation, linking, or loading are logged, and the process
     * continues for other scripts.
     *
     * @return `true` if any errors occurred during the entire process, `false` otherwise.
     * @since 1.0.0
     */
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
        val compilationResults = mutableListOf<ScriptCompiler.CompilationResult>()

        logger.info("Now compiling utility scripts...")

        for (file in utilityScripts) {
            jobs += coroutineScope.launch {
                try {
                    logger.info("[${file.name}] Compiling...")
                    val result = ScriptCompiler.compileUtilityScript(file.toFile())
                    logger.info("[${file.name}] Done compiling: ${result?.classLoader}")

                    if (result != null) {
                        compilationResults += result
                    } else {
                        logger.warning("[${file.name}] No ClassLoader found.")
                    }
                } catch (e: Exception) {
                    logger.severe("[${file.name}] Failed to compile. (${e.javaClass.simpleName})")
                    errs = true
                }
            }
        }

        jobs.joinAll()
        jobs.clear()

        logger.info("Resolved ${compilationResults.size} utility script ClassLoaders.")
        logger.info("Now generating temp artifacts and full ClassLoader...")

        val fullClassLoader = ExtendableClassLoader(Backbone::class.java.classLoader)
        val jarFiles = mutableListOf<File>()
        val newDefaultImports = mutableListOf(
            "kotlin.script.experimental.dependencies.DependsOn",
            "kotlin.script.experimental.dependencies.Repository",
            "net.integr.backbone.systems.hotloader.annotations.CompilerOptions"
        )

        for (result in compilationResults) {
            val entries = ScriptCompiler.getClassloaderEntries(result.classLoader)
            val tempJar = ScriptCompiler.createTempJar(entries)

            fullClassLoader.addClasses(entries)

            for (file in result.classPath) {
                fullClassLoader.addURL(file.toURI().toURL())
            }

            newDefaultImports += "${result.fqName}.*"

            jarFiles += tempJar
        }

        logger.info("Now compiling scripts...")

        val newScripts = ConcurrentHashMap<String, ScriptStore.State>()

        val compilationConfig = createJvmCompilationConfigurationFromTemplate<Script>().with {
            jvm {
                defaultImports(newDefaultImports)
            }

            hostConfiguration(ScriptingHostConfiguration {
                jvm {
                    baseClassLoader(fullClassLoader)
                }
            })

            updateClasspath(jarFiles)
        }

        val evaluationConfig = createJvmEvaluationConfigurationFromTemplate<Script>().with {
            jvm {
                baseClassLoader(fullClassLoader)
            }
        }

        val epoch = reloadEpoch.incrementAndFetch()

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
                        logger.info("[${file.name}] Transferred state from old script.")

                        // Leak detection
                        ProbeHandler.register(file.name, epoch, oldLifecycle)
                    }

                    newScripts[file.name] = ScriptStore.State(false, lifecycle)

                } catch (e: Exception) {
                    logger.severe("[${file.name}] Failed to compile. (${e.message})")
                    errs = true
                }
            }
        }

        jobs.joinAll()

        logger.info("Compiled ${newScripts.size} scripts.")
        logger.info("Now swapping hot...")

        // Close old classloaders to prevent classloader retention leaks
        logger.info("Closing ${oldClassLoaders.size} old classloaders...")
        for (oldLoader in oldClassLoaders) {
            try {
                withContext(Dispatchers.IO) {
                    oldLoader.close()
                }
            } catch (e: Exception) {
                logger.warning("Failed to close old classloader: ${e.message}")
            }
        }

        oldClassLoaders.clear()

        // Store the new fullClassLoader for cleanup on next reload
        oldClassLoaders.add(fullClassLoader)

        val unloadErrs = unloadScripts()
        errs = errs || unloadErrs
        logger.info("Unloaded old scripts.")

        logger.info("Enabling ${newScripts.size} scripts...")

        for ((name, state) in newScripts) {
            try {
                state.lifecycle.load()
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