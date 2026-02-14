package net.integr.backbone.systems.hotloader.configuration

import kotlin.script.experimental.annotations.KotlinScript


@KotlinScript(
    fileExtension = "bb.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
abstract class Script