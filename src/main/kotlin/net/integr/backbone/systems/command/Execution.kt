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
import org.jetbrains.annotations.ApiStatus

/**
 * Represents the context of a command execution, providing access to the sender, arguments, and utility methods.
 *
 * @param sender The sender of the command.
 * @param args A map of argument names to their parsed values.
 * @param format The command feedback format to use for responses.
 *
 * @since 1.0.0
 */
class Execution @ApiStatus.Internal constructor(val sender: CommandSender?, val args: Map<String, Any>, private val format: CommandFeedbackFormat) {
    /**
     *
     * Retrieves the value of an argument by its name, casting it to the specified type.
     *
     * @param key The name of the argument.
     * @return The value of the argument, cast to the specified type.
     * @throws ClassCastException If the argument's value cannot be cast to the specified type.
     * @since 1.0.0
     */
    fun <T> get(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return args[key] as T
    }

    /**
     * Sends a formatted message back to the command sender.
     *
     * @param message The message to send.
     * @since 1.0.0
     */
    fun respond(message: String) {
        sender?.sendMessage(format.format(message))
    }

    /**
     * Sends a formatted success message back to the command sender.
     *
     * @param message The success message to send.
     * @since 1.8.1
     */
    fun respondSuccess(message: String) {
        sender?.sendMessage(format.formatSuccess(message))
    }

    /**
     * Sends a formatted warning message back to the command sender.
     *
     * @param message The warning message to send.
     *
     * @since 1.8.1
     */
    fun respondWarning(message: String) {
        sender?.sendMessage(format.formatWarning(message))
    }

    /**
     * Sends a formatted error message back to the command sender.
     *
     * @param message The error message to send.
     * @since 1.8.1
     */
    fun respondError(message: String) {
        sender?.sendMessage(format.formatError(message))
    }

    /**
     *
     * Sends a formatted component message back to the command sender.
     *
     * @param component The component to send.
     * @since 1.0.0
     */
    fun respondComponent(component: Component) {
        sender?.sendMessage(component)
    }

    /**
     * Fails this command execution if the sender is a Player.
     *
     * @throws CommandFailedException If the sender is a Player.
     * @since 1.0.0
     */
    fun failOnPlayer() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    /**
     * Fails this command execution if the sender isn't a Player.
     *
     * @throws CommandFailedException If the sender isn't a Player.
     * @since 1.0.0
     */
    fun requirePlayer() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    /**
     * Fails this command execution if the sender is a Console.
     *
     * @throws CommandFailedException If the sender is a Console.
     * @since 1.0.0
     */
    fun failOnConsole() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    /**
     * Fails this command execution if the sender isn't a Console.
     *
     * @throws CommandFailedException If the sender isn't a Console.
     * @since 1.0.0
     */
    fun requireConsole() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    /**
     *
     * Retrieves the sender as a Player, failing if the sender is not a player.
     *
     * @return The sender as a Player.
     * @throws CommandFailedException If the sender is not a player.
     * @since 1.0.0
     */
    fun getPlayer(): Player {
        requirePlayer()
        return sender as Player
    }

    /**
     * Retrieves the sender as a Console, failing if the sender is not a console.
     *
     * @return The sender as a Console.
     * @throws CommandFailedException If the sender is not a console.
     * @since 1.0.0
     */
    fun getConsole(): CommandSender {
        requireConsole()
        return sender!!
    }

    /**
     *
     * Fails the command execution with the given message.
     *
     * @param message The error message.
     * @throws CommandFailedException Always throws this exception with the provided message.
     * @since 1.0.0
     */
    fun fail(message: String): Nothing {
        throw CommandFailedException(message)
    }

    /**
     * Requires the sender to have a specific permission.
     *
     * @param perm The permission node to check.
     * @throws CommandFailedException If the sender does not have the required permission.
     * @since 1.0.0
     */
    fun requirePermission(perm: PermissionNode) {
        if (!sender!!.hasPermission(perm.id)) {
            fail("You do not have permission to execute this command.")
        }
    }

    /**
     * Requires the sender to not have a specific permission.
     *
     * @param perm The permission node to check.
     * @throws CommandFailedException If the sender has the required permission.
     * @since 1.0.0
     */
    fun cantHavePermission(perm: PermissionNode) {
        if (sender!!.hasPermission(perm.id)) {
            fail("You are not allowed to execute this command.")
        }
    }

    /**
     * Checks if the sender has a specific permission.
     *
     * @param perm The permission node to check.
     * @return True if the sender has the permission, false otherwise.
     * @since 1.0.0
     */
    fun hasPermission(perm: PermissionNode): Boolean {
        return sender!!.hasPermission(perm.id)
    }
}