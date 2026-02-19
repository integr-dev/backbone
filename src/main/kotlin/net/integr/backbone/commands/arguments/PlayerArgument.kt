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

package net.integr.backbone.commands.arguments

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.argument.Argument
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun playerArgument(name: String, description: String): Argument<Player> {
    return PlayerArgument(name, description)
}

class PlayerArgument(name: String, description: String) : Argument<Player>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()
        val players = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()

        return if (arg.text.isBlank()) {
            CompletionResult(mutableListOf("<$name:player>", *players.toTypedArray()), arg.end)
        } else {
            CompletionResult(players, arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<Player> {
        val arg = current.getNextSingle()

        val foundPlayer = Bukkit.getPlayer(arg.text)
            ?: throw CommandArgumentException("Argument '$name' must be a valid online player.")

        return ParseResult(foundPlayer, arg.end)
    }
}