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
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.*
import kotlin.io.path.appendText
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

/**

 * A logger that uses the Bukkit plugin's logger as a parent, for compatibility mode.
 * This logger will log messages to the console using the plugin's logger, and can be derived to create sub-loggers.
 *
 * @param name The name of the logger.
 * @param plugin The plugin to use as a parent logger. If null, this logger will not have a parent and will log to the console directly.
 *
 * @since 1.8.0
 */
class BackboneCompatibilityLogger(name: String, private val plugin: JavaPlugin?) : Logger(name, null) {
    init {
        if (plugin != null) setParent(plugin.logger)
        setLevel(Level.ALL)
    }
}