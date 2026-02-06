package net.integr.backbone.systems.hotloader.configuration

import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.annotations.CompilerOptions
import kotlin.script.experimental.annotations.KotlinScript
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


@KotlinScript(
    fileExtension = "bbu.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
interface UtilityScript

@KotlinScript(
    fileExtension = "bb.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
abstract class Script

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ScriptConfiguration : ScriptCompilationConfiguration({ // To make the ide shut up
    jvm {
        defaultImports(DependsOn::class, Repository::class, CompilerOptions::class)
        dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, CompilerOptions::class, handler = ImportHandler())
    }
})

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object EvaluationConfiguration : ScriptEvaluationConfiguration({

})