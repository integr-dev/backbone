/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.backbone.systems.hotloader.configuration

import org.jetbrains.annotations.ApiStatus
import kotlin.script.experimental.annotations.KotlinScript

/**
 * Base class for Backbone scripts.
 *
 * This class serves as the entry point for all Kotlin scripts intended to be loaded and executed by Backbone.
 * It defines the file extension, compilation configuration, and evaluation configuration for these scripts.
 *
 * @since 1.0.0
 */
@KotlinScript(
    fileExtension = "bb.kts",
    compilationConfiguration = ScriptConfiguration::class,
    evaluationConfiguration = EvaluationConfiguration::class
)
@ApiStatus.Internal
abstract class Script