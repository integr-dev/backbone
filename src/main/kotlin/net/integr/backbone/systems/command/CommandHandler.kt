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

package net.integr.backbone.systems.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.integr.backbone.Backbone
import net.integr.backbone.text.formats.CommandFeedbackFormat
import org.bukkit.command.CommandMap
import java.awt.Color
import java.lang.reflect.Field


object CommandHandler {
    val logger = Backbone.LOGGER.derive("command-handler")
    val defaultFeedbackFormat = CommandFeedbackFormat("backbone", Color(141, 184, 130))

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val map: CommandMap by lazy {
        logger.info("Got command map via reflection.")
        val bukkitCommandMap: Field = Backbone.SERVER.javaClass.getDeclaredField("commandMap")
        bukkitCommandMap.isAccessible = true
        bukkitCommandMap.get(Backbone.SERVER) as CommandMap
    }


    fun register(command: Command, prefix: String = "backbone") {
        command.build()
        map.register(prefix, command)
        Backbone.SERVER.onlinePlayers.forEach {
            it.updateCommands()
        }
    }

    fun unregister(command: Command, prefix: String = "backbone") {
        unregisterCommand(command.name, prefix)

        Backbone.SERVER.onlinePlayers.forEach {
            it.updateCommands()
        }
    }

    fun unregisterCommand(commandName: String, prefix: String = "backbone") {
        try {
            val knownCommandsField = map.javaClass.getSuperclass().getDeclaredField("knownCommands")
            knownCommandsField.setAccessible(true)
            val knownCommands = knownCommandsField.get(map) as MutableMap<*, *>

            knownCommands.remove(commandName)
            knownCommands.remove("$prefix:$commandName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}