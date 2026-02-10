package net.integr.backbone.systems.hotloader.configuration

import kotlinx.coroutines.runBlocking
import net.integr.backbone.systems.hotloader.annotations.CompilerOptions
import net.integr.backbone.systems.hotloader.configuration.resolver.AnnotationResolver
import net.integr.backbone.systems.hotloader.configuration.resolver.IvyResolver
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.dependencies.CompoundDependenciesResolver
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.updateClasspath

class RefinementHandler : RefineScriptCompilationConfigurationHandler {
    private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), IvyResolver())

    override operator fun invoke(context: ScriptConfigurationRefinementContext):
            ResultWithDiagnostics<ScriptCompilationConfiguration> = processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val diagnostics = arrayListOf<ScriptDiagnostic>()

        val annotations = context.collectedData
            ?.get(ScriptCollectedData.Companion.foundAnnotations)
            ?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val compileOptions = annotations.flatMap {
            (it as? CompilerOptions)?.options?.toList() ?: emptyList()
        }

        val resolveResult = try {
            runBlocking {
                AnnotationResolver.resolveFromAnnotations(
                    resolver,
                    annotations.filter { it is DependsOn || it is Repository })
            }
        } catch (e: Throwable) {
            ResultWithDiagnostics.Failure(*diagnostics.toTypedArray(), e.asDiagnostics(path = context.script.locationId))
        }

        return resolveResult.onSuccess { resolvedClassPath ->
            ScriptCompilationConfiguration(context.compilationConfiguration) {
                updateClasspath(resolvedClassPath)
                if (compileOptions.isNotEmpty()) compilerOptions.append(compileOptions)
            }.asSuccess()
        }
    }
}