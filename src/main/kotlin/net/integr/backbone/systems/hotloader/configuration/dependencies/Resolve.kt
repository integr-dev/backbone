package net.integr.backbone.systems.hotloader.configuration.dependencies

import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.flatMapSuccess
import kotlin.script.experimental.api.makeFailureResult
import kotlin.script.experimental.api.valueOr
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.dependencies.addRepository
/*
    This file originates from the JetBrains simple-main-kts example for kotlin scripting
    https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/simple-main-kts/simple-main-kts/src/main/kotlin/org/jetbrains/kotlin/script/examples/simpleMainKts/impl/resolve.kt
 */
suspend fun resolveFromAnnotations(resolver: ExternalDependenciesResolver, annotations: Iterable<Annotation>): ResultWithDiagnostics<List<File>> {
    val reports = mutableListOf<ScriptDiagnostic>()
    annotations.forEach { annotation ->
        when (annotation) {
            is Repository -> {
                for (coordinates in annotation.repositoriesCoordinates) {
                    val added = resolver.addRepository(coordinates)
                        .also { reports.addAll(it.reports) }
                        .valueOr { return it }

                    if (!added)
                        return makeFailureResult(
                            "Unrecognized repository coordinates: $coordinates"
                        )
                }
            }
            is DependsOn -> {}
            else -> return makeFailureResult("Unknown annotation ${annotation.javaClass}")
        }
    }
    return annotations.filterIsInstance<DependsOn>().flatMapSuccess { annotation ->
        annotation.artifactsCoordinates.asIterable().flatMapSuccess { artifactCoordinates ->
            resolver.resolve(artifactCoordinates)
        }
    }
}