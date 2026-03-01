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

import net.integr.backbone.Backbone
import net.integr.backbone.systems.hotloader.annotations.CompilerOptions
import org.jetbrains.annotations.ApiStatus
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

/**
 * Defines the compilation configuration for Backbone scripts.
 *
 * This object configures the Kotlin script compilation process, including:
 * - JVM settings: Specifies the classloader for dependencies and sets the JVM target version.
 * - IDE settings: Allows scripts to be accepted in any location within the IDE.
 * - Refinement: Uses [RefinementHandler] to process annotations like [DependsOn], [Repository], and [CompilerOptions]
 *   to dynamically adjust the compilation configuration.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
        defaultImports(DependsOn::class, Repository::class, CompilerOptions::class)
        compilerOptions("-jvm-target", "21")
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

/**
 * Defines the evaluation configuration for Backbone scripts.
 * This object currently uses the default evaluation configuration provided by the Kotlin scripting API.
 * It is marked as internal and suppressible for Java serialization warnings.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object EvaluationConfiguration : ScriptEvaluationConfiguration()