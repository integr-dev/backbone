package net.integr.backbone.systems.hotloader.configuration

import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.annotations.CompilerOptions
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
        defaultImports(DependsOn::class, Repository::class, CompilerOptions::class)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    refineConfiguration {
        onAnnotations(
            handler = RefinementHandler(),
            annotations = listOf(
                DependsOn::class,
                Repository::class,
                CompilerOptions::class
            )
        )
    }
})

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object EvaluationConfiguration : ScriptEvaluationConfiguration()