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

import kotlinx.coroutines.runBlocking
import net.integr.backbone.BackboneLogger
import net.integr.backbone.systems.hotloader.configuration.UtilityScript
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.impl.getOrCreateActualClassloader
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

/**
 * Utility object for compiling Kotlin scripts within the Backbone hot-reloading system.
 *
 * This object provides methods to compile both standard Backbone scripts (expected to return [ManagedLifecycle] instances)
 * and utility scripts (which might return arbitrary results). It leverages the Kotlin scripting API to perform
 * compilation and evaluation, handles logging of diagnostic messages, and provides utilities for managing classloaders
 * and creating temporary JAR files from compiled classes.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
object ScriptCompiler {
    /**
     * Reports a successful compilation result, including the resolved classpath, the classloader used, and the fully qualified name of the compiled script class.
     * @property classPath The list of files representing the resolved classpath for the compiled script.
     * @property classLoader The [ClassLoader] instance used to load the compiled script.
     * @property fqName The fully qualified name of the main script class.
     */
    data class CompilationResult(val classPath: List<File>, val classLoader: ClassLoader, val fqName: String)

    private val logger = ScriptEngine.logger.derive("compiler")

    /**
     * A simple scripting host for compiling and evaluating Kotlin scripts.
     * @since 1.0.0
     */
    private val scriptingHost = BasicJvmScriptingHost()

    /**
     * Compile and evaluate a Backbone script.
     *
     * This method compiles and evaluates a given script file. It expects the script to return an instance
     * of [ManagedLifecycle]. If the script does not return a [ManagedLifecycle] object, an
     * [IllegalStateException] is thrown.
     *
     * @param file The script file to compile and evaluate.
     * @param compilationConfiguration The [ScriptCompilationConfiguration] to use for compilation.
     * @param evaluationConfiguration The [ScriptEvaluationConfiguration] to use for evaluation.
     * @return The [ManagedLifecycle] instance returned by the evaluated script.
     * @throws IllegalStateException If the script does not return a [ManagedLifecycle] object.
     * @since 1.0.0
     */
    fun compileScript(
        file: File,
        compilationConfiguration: ScriptCompilationConfiguration,
        evaluationConfiguration: ScriptEvaluationConfiguration
    ): ManagedLifecycle {
        logger.info("[${file.name}] Evaluating...")
        val result = scriptingHost.eval(
            script = file.toScriptSource(),
            compilationConfiguration = compilationConfiguration,
            evaluationConfiguration = evaluationConfiguration
        )

        logReports(file, result.reports)

        val evalValue = result.valueOrNull()?.returnValue

        if (evalValue is ResultValue.Value && evalValue.value is ManagedLifecycle) {
            return evalValue.value as ManagedLifecycle
        } else {
            throw IllegalStateException(
                "Script did not return a ManagedLifecycle object. Found: $evalValue"
            )
        }
    }

    /**
     * Compiles a utility script.
     *
     * This method compiles a given utility script file and returns a [CompilationResult] containing
     * the resolved classpath, the classloader used, and the fully qualified name of the compiled script class.
     * Utility scripts are not evaluated directly by this method; instead, their compiled form is returned
     * for later use.
     *
     * @param file The utility script file to compile.
     * @return A [CompilationResult] if compilation is successful, or `null` if it fails.
     * @since 1.0.0
     */
    fun compileUtilityScript(file: File): CompilationResult? {
        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<UtilityScript>()
        val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<UtilityScript>()

        return runBlocking {
            logger.info("[${file.name}] Evaluating...")
            val result = scriptingHost.compiler.invoke(
                script = file.toScriptSource(),
                scriptCompilationConfiguration = compilationConfiguration
            )

            logReports(file, result.reports)

            val value = result.valueOrNull()

            if (value is KJvmCompiledScript) {
                logger.info("[${file.name}] Grabbing ClassLoader...")
                val classLoader = value.getOrCreateActualClassloader(evaluationConfiguration)

                val classpath = value.compilationConfiguration[ScriptCompilationConfiguration.dependencies]
                    ?.filterIsInstance<JvmDependency>()?.flatMap { it.classpath } ?: emptyList()

                val result = CompilationResult(classpath, classLoader, value.scriptClassFQName)

                return@runBlocking result
            } else return@runBlocking null
        }
    }

    /**
     * Logs diagnostic reports from script compilation or evaluation.
     *
     * This method iterates through a list of [ScriptDiagnostic] reports and logs them using the
     * internal logger. Warnings and errors are highlighted with different colors, and their
     * location within the script is made human-readable. If an exception is associated with
     * a report, its stack trace is printed.
     *
     * @param file The script file associated with the reports.
     * @param reports The list of [ScriptDiagnostic] reports to log.
     * @since 1.0.0
     */
    private fun logReports(file: File, reports: List<ScriptDiagnostic>) {
        reports.forEach { report ->
            if (report.severity >= ScriptDiagnostic.Severity.WARNING) {
                logger.warning(
                    "[${file.name}] [${getSeverityColor(report.severity)}${report.severity}${BackboneLogger.CustomFormat.ANSI_RESET}] ${report.message} (${getLocationReadable(report.location)})"
                )

                report.exception?.printStackTrace()
            }
        }
    }

    /**
     * Matches the severity of a diagnostic report to an ANSI color code for console output.
     *
     * @param sev The [ScriptDiagnostic.Severity] to get the color for.
     * @return An ANSI color code string.
     * @since 1.0.0
     */
    private fun getSeverityColor(sev: ScriptDiagnostic.Severity): String {
        return when (sev) {
            ScriptDiagnostic.Severity.WARNING -> BackboneLogger.CustomFormat.ANSI_YELLOW
            ScriptDiagnostic.Severity.ERROR -> BackboneLogger.CustomFormat.ANSI_RED
            else -> BackboneLogger.CustomFormat.ANSI_CYAN
        }
    }

    /**
     * Converts a [SourceCode.Location] object into a human-readable string representation.
     *
     * This method formats the location to show the starting line and column, and optionally
     * the ending line and column if available.
     *
     * @param location The [SourceCode.Location] to convert.
     * @return A string representing the location (e.g., "1:10" or "1:10 - 2:5").
     * @since 1.0.0
     */
    private fun getLocationReadable(location: SourceCode.Location?): String {
        if (location == null) return "Unknown"
        return "${location.start.line}:${location.start.col}" + if (location.end != null) " - ${location.end!!.line}:${location.end!!.col}" else ""
    }

    /**
     * Retrieves the class entries (class name to byte array mapping) from a given [ClassLoader].
     *
     * This method uses reflection to access the `entries` field of the provided class loader,
     * which is assumed to be an [ExtendableClassLoader] or a similar custom class loader
     * that stores class definitions as byte arrays.
     *
     * @param loader The [ClassLoader] from which to retrieve class entries.
     * @return A map where keys are class names (e.g., "com/example/MyClass.class") and values are
     *         the byte arrays representing the class definitions.
     * @since 1.0.0
     */
    fun getClassloaderEntries(loader: ClassLoader): Map<String, ByteArray> {
        val entriesField = loader.javaClass
            .getDeclaredField("entries")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        return entriesField.get(loader) as Map<String, ByteArray>
    }

    /**
     * Creates a temporary JAR file containing the provided class entries.
     *
     * This method takes a map of class names to their byte array definitions and packages them
     * into a temporary JAR file. This JAR file can then be added to a classloader's classpath.
     *
     * @param entries A map where keys are class names (e.g., "com/example/MyClass.class") and values are
     *                the byte arrays representing the class definitions.
     * @return A [File] object representing the created temporary JAR file.
     * @since 1.0.0
     */
    fun createTempJar(entries: Map<String, ByteArray> ): File {
        val tempJar = File.createTempFile("script-deps-", ".jar")

        JarOutputStream(FileOutputStream(tempJar)).use { jos ->
            entries.forEach { (path, bytes) ->
                jos.putNextEntry(JarEntry(path))
                jos.write(bytes)
                jos.closeEntry()
            }
        }

        return tempJar
    }
}