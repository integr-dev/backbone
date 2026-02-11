package net.integr.backbone.systems.hotloader.configuration.resolver

import net.integr.backbone.systems.hotloader.configuration.RefinementHandler
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

object AnnotationResolver {
    val logger = RefinementHandler.logger.derive("resolver")

    suspend fun resolveFromAnnotations(
        resolver: ExternalDependenciesResolver,
        annotations: Iterable<Annotation>
    ): ResultWithDiagnostics<List<File>> {
        val reports = mutableListOf<ScriptDiagnostic>()
        annotations.forEach { annotation ->
            when (annotation) {
                is Repository -> {
                    logger.info("Adding repository: ${annotation.repositoriesCoordinates}")

                    for (coordinates in annotation.repositoriesCoordinates) {
                        val added = resolver.addRepository(coordinates)
                            .also { reports.addAll(it.reports) }
                            .valueOr { return it }

                        if (!added) {
                            logger.warning("Could not add repository: $coordinates")
                            return makeFailureResult("Unrecognized repository coordinates: $coordinates")
                        }
                    }
                }
                is DependsOn -> {}
                else -> return makeFailureResult("Unknown annotation ${annotation.javaClass}")
            }
        }

        return annotations.filterIsInstance<DependsOn>().flatMapSuccess { annotation ->
            annotation.artifactsCoordinates.asIterable().flatMapSuccess { artifactCoordinates ->
                logger.info("Processing dependency: $artifactCoordinates")
                val res = resolver.resolve(artifactCoordinates)
                logger.info("Processed dependency: $artifactCoordinates")
                res
            }
        }
    }
}
