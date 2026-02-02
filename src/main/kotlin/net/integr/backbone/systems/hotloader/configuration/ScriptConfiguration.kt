package net.integr.backbone.systems.hotloader.configuration

import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.annotations.Import
import java.io.File
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfigurationRefinementContext
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.api.refineConfigurationBeforeEvaluate
import kotlin.script.experimental.api.with
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.mainArguments

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    refineConfiguration {
        onAnnotations(Import::class, handler = ImportHandler())
    }
})

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object EvaluationConfiguration : ScriptEvaluationConfiguration({})



class ImportHandler : RefineScriptCompilationConfigurationHandler {
    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> =
        processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
        val importedSources = annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            } ?: emptyList()
        }

        return ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (importedSources.isNotEmpty()) importScripts.append(importedSources)
        }.asSuccess()
    }
}

