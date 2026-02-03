package net.integr.backbone.systems.hotloader.configuration

import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import kotlin.script.experimental.annotations.KotlinScript


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
abstract class Script : ManagedLifecycle()