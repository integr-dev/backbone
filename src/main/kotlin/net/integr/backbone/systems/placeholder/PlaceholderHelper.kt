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
import org.bukkit.entity.Player

/**
 * A utility object for interacting with PlaceholderAPI.

 * This helper provides a simplified method for applying PlaceholderAPI placeholders
 * to a given string for a specific player. It gracefully handles cases where
 * PlaceholderAPI is not installed or enabled.
 *
 * @since 1.0.0
 */
object PlaceholderHelper {
    private val logger = Backbone.LOGGER.derive("placeholder-helper")

    /**
     * Applies PlaceholderAPI placeholders to a given string for a specific player.
     *
     * If PlaceholderAPI is installed and enabled, this method will parse the input string
     * and replace any valid placeholders with their corresponding values based on the
     * provided player context. If PlaceholderAPI is not available, a warning will be logged,
     * and the original string will be returned unchanged.
     *
     * @param player The [Player] for whom the placeholders should be resolved.
     * @param text The input string potentially containing PlaceholderAPI placeholders.
     * @return The string with placeholders resolved, or the original string if PlaceholderAPI is not available.
     * @since 1.0.0
     */
    fun fill(player: Player, text: String): String {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text)
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
            return text
        }
    }
}