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

package net.integr.backbone.systems.hotloader

import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * A unified store for all hot-loaded scripts and their associated states.
 *
 * This object acts as a central registry for all scripts managed by the hot-reloading system.
 * It stores [State] objects, which encapsulate whether a script is enabled and its
 * [ManagedLifecycle] instance. It provides methods for retrieving script names,
 * accessing scripts by name, and filtering scripts based on their enabled status.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
object ScriptStore {
    /**
     * Represents the current state of a hot-loaded script.
     *
     * @property enabled `true` if the script is currently enabled and active, `false` otherwise.
     * @property lifecycle The [ManagedLifecycle] instance associated with this script,
     *                     providing access to its lifecycle methods and sustained states.
     * @since 1.0.0
     */
    class State(var enabled: Boolean, var lifecycle: ManagedLifecycle, var attachedLoader: ClassLoader)

    /**
     * A thread-safe map to store all currently loaded scripts.
     * The key is the script's file name (e.g., "MyScript.bb.kts"), and the value is its [State].
     *
     * @since 1.0.0
     */
    var scripts = ConcurrentHashMap<String, State>()

    /**
     * Get the names of all currently loaded scripts.
     *
     * This method extracts the base name of each script (without the ".bb.kts" extension)
     * from the keys of the [scripts] map.
     *
     * @return A list of strings, where each string is the name of a loaded script.
     * @since 1.0.0
     */
    fun getScriptNames() = scripts.map { it.key.substringBefore(".bb.kts") }

    /**
     * Retrieves the [State] object for a script by its base name (without the file extension).
     *
     * @param name The base name of the script (e.g., "MyScript").
     * @return The [State] object for the specified script, or `null` if not found.
     * @since 1.0.0
     */
    fun getScriptByName(name: String): State? {
        val key = scripts.keys.firstOrNull { it.substringBefore(".bb.kts") == name }
        return scripts[key]
    }

    /**
     * Retrieves a list of names of all currently enabled scripts.
     *
     * @return A list of strings, where each string is the name of an enabled script.
     * @since 1.0.0
     */
    fun getEnabledScripts(): List<String> {
        return scripts.filter { it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

    /**
     * Retrieves a list of names of all currently disabled scripts.
     *
     * @return A list of strings, where each string is the name of a disabled script.
     * @since 1.0.0
     */
    fun getDisabledScripts(): List<String> {
        return scripts.filter { !it.value.enabled }.map { it.key.substringBefore(".bb.kts") }
    }

}