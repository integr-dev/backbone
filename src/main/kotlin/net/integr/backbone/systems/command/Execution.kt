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

import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.text.formats.CommandFeedbackFormat
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Execution(val sender: CommandSender?, val args: Map<String, Any>, private val format: CommandFeedbackFormat) {
    fun <T> get(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return args[key] as T
    }

    fun respond(message: String) {
        sender?.sendMessage(format.format(message))
    }

    fun respondComponent(component: Component) {
        sender?.sendMessage(component)
    }

    fun failOnPlayer() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    fun requirePlayer() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    fun failOnConsole() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    fun requireConsole() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    fun getPlayer(): Player {
        requirePlayer()
        return sender as Player
    }

    fun getConsole(): CommandSender {
        requireConsole()
        return sender!!
    }

    fun fail(message: String): Boolean {
        throw CommandFailedException(message)
    }

    fun requirePermission(perm: PermissionNode) {
        if (!sender!!.hasPermission(perm.id)) {
            fail("You do not have permission to execute this command.")
        }
    }

    fun cantHavePermission(perm: PermissionNode) {
        if (sender!!.hasPermission(perm.id)) {
            fail("You are not allowed to execute this command.")
        }
    }

    fun hasPermission(perm: PermissionNode): Boolean {
        return sender!!.hasPermission(perm.id)
    }
}