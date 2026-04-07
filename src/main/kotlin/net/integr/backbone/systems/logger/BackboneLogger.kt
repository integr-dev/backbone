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

package net.integr.backbone.systems.logger

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.LogRecord
import java.util.logging.Logger

/**

 * A logger for the Backbone plugin that supports both a custom logging implementation and a compatibility mode for older versions of Minecraft.
 * The logger can be derived to create sub-loggers with specific names, and it can log messages to both the console and a file.
 *
 * @param name The name of the logger.
 * @param compatibilityMode If true, the logger will use a compatibility mode that is compatible with older versions of Minecraft. If false, it will use a custom logging implementation.
 * @param plugin An optional reference to the JavaPlugin instance, required for compatibility mode to access the plugin's logger.
 *
 * @since 1.0.0
 */
class BackboneLogger(name: String, val compatibilityMode: Boolean = false, private val plugin: JavaPlugin? = null) : Logger(name, null) {
    val backing = if (compatibilityMode) BackboneCompatibilityLogger(name, plugin) else BackboneCustomLogger(name)

    override fun log(record: LogRecord?) {
        backing.log(record)
    }

    /**
     * Derive a new logger with a sub-name.
     *
     * @param subName The sub-name for the new logger.
     * @return A new `BackboneLogger` instance.
     *
     * @since 1.0.0
     */
    fun derive(subName: String): BackboneLogger {
        return BackboneLogger("$name.$subName", compatibilityMode, plugin)
    }
}