package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.runBlocking
import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.ScriptEngine.logger
import net.integr.backbone.systems.hotloader.configuration.UtilityScript
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import java.io.File
import java.io.FileOutputStream
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.impl.getOrCreateActualClassloader
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object ScriptCompiler {
    val logger = Backbone.LOGGER.derive("script-compiler")

    private val scriptingHost = BasicJvmScriptingHost()

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
                "[${file.name}] Script did not return a ManagedLifecycle object. Found: $evalValue"
            )
        }
    }

    fun compileUtilityScript(file: File): ClassLoader? {
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
                logger.info("[${file.name}] Grabbing classloader...")
                val classLoader = value.getOrCreateActualClassloader(evaluationConfiguration)

                return@runBlocking classLoader
            } else return@runBlocking null
        }
    }

    fun logReports(file: File, reports: List<ScriptDiagnostic>) {
        reports.forEach { report ->
            if (report.severity >= ScriptDiagnostic.Severity.WARNING) {
                logger.warning(
                    "[${file.name}] [${report.severity}] ${report.message} (${report.location})"
                )

                report.exception?.printStackTrace()
            }
        }
    }

    fun getClassloaderEntries(loader: ClassLoader): Map<String, ByteArray> {
        val entriesField = loader.javaClass
            .getDeclaredField("entries")
            .apply { isAccessible = true }

        @Suppress("UNCHECKED_CAST")
        return entriesField.get(loader) as Map<String, ByteArray>
    }

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