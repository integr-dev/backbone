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

import net.integr.backbone.Backbone
import org.jetbrains.annotations.ApiStatus

/**
 * A central object for managing the lifecycle of hot-loaded scripts.
 *
 * This object provides functionalities to unload, disable, enable, and wipe the state of scripts
 * managed by the hot-reloading system. It interacts with [ScriptStore] to access script states
 * and invokes the lifecycle methods (onLoad, onUnload, wipeStates) defined in [net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle]
 * implementations.
 *
 * @since 1.0.0
 */
@ApiStatus.Internal
object ScriptEngine {
    val logger = Backbone.LOGGER.derive("script-engine")

    /**
     * Unloads all currently loaded scripts.
     *
     * This method iterates through all scripts stored in [ScriptStore.scripts],
     * invokes their `onUnload` method, marks them as disabled, and then clears
     * the [ScriptStore.scripts] map. It logs any errors encountered during the
     * unloading process.
     *
     * @return `true` if any errors occurred during unloading, `false` otherwise.
     * @since 1.0.0
     */
    fun unloadScripts(): Boolean {
        var errs = false

        for ((name, state) in ScriptStore.scripts) {
            try {
                state.lifecycle.onUnload()
                state.enabled = false
                logger.info("Unloading script: $name")
            } catch (e: Exception) {
                logger.severe("Failed to unload script: $name")
                e.printStackTrace()
                errs = true
            }
        }

        ScriptStore.scripts.clear()
        return errs
    }

    /**
     * Disable a script by name.
     *
     * This method invokes the `onUnload` method of the specified script,
     * marks it as disabled, and logs the action. If the script is not found
     * or is already disabled, an [IllegalArgumentException] is thrown.
     * Any exceptions during unloading are caught, logged, and re-thrown
     * as an [IllegalArgumentException].
     *
     * @param name The name of the script to disable.
     * @throws IllegalArgumentException If the script is not found, already disabled,
     *                                  or an error occurs during unloading.
     * @since 1.0.0
     */
    fun disableScript(name: String) {
        val state = ScriptStore.getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (!state.enabled) {
            logger.warning("Script '$name' is already disabled.")
            throw IllegalArgumentException("Script '$name' is already disabled.")
        }

        try {
            script.onUnload()
            state.enabled = false
            logger.info("Disabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to disable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while disabling script.")
        }
    }

    /**
     * Enable a script by name.
     *
     * This method invokes the `onLoad` method of the specified script,
     * marks it as enabled, and logs the action. If the script is not found
     * or is already enabled, an [IllegalArgumentException] is thrown.
     * Any exceptions during loading are caught, logged, and re-thrown
     * as an [IllegalArgumentException].
     *
     * @param name The name of the script to enable.
     * @throws IllegalArgumentException If the script is not found, already enabled,
     *                                  or an error occurs during loading.
     * @since 1.0.0
     */
    fun enableScript(name: String) {
        val state = ScriptStore.getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (state.enabled) {
            logger.warning("Script '$name' is already enabled.")
            throw IllegalArgumentException("Script '$name' is already enabled.")
        }

        try {
            script.onLoad()
            state.enabled = true
            logger.info("Enabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to enable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while enabling script.")
        }
    }

    /**
     * Wipes the sustained state of a script by name.
     *
     * This method invokes the `wipeStates` method of the specified script,
     * resetting all its [net.integr.backbone.systems.hotloader.lifecycle.LifecycleSustainedState]
     * properties to their default values. It then logs the action.
     * If the script is not found, an [IllegalArgumentException] is thrown.
     * Any exceptions during the wiping process are caught, logged, and re-thrown
     * as an [IllegalArgumentException].
     *
     * @param name The name of the script whose state to wipe.
     * @throws IllegalArgumentException If the script is not found or an error occurs during wiping.
     * @since 1.0.0
     */
    fun wipeScript(name: String) {
        val state = ScriptStore.getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        try {
            script.wipeStates()
            state.enabled = true
            logger.info("Wiped script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to wipe script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while enabling script.")
        }
    }
}