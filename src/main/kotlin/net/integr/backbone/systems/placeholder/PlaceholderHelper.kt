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

package net.integr.backbone.systems.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import net.integr.backbone.Backbone
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlaceholderHelper {
    val logger = Backbone.LOGGER.derive("placeholder-helper")

    fun fill(player: Player, text: String): String {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text)
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
            return text
        }
    }
}