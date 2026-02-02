package net.integr.backbone.systems.hotloader.configuration

import kotlin.script.experimental.annotations.KotlinScript


@KotlinScript(
    fileExtension = "util.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
interface UtilityScript

@KotlinScript(
    fileExtension = "bb.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
interface Script