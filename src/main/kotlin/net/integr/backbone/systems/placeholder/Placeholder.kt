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

import org.bukkit.entity.Player

/**
 * Represents a single placeholder, which can be registered to a PlaceholderGroup.
 * @param id The unique identifier for this placeholder, which will be used in the placeholder string (e.g. "player_name" for "%group_player_name%").
 * @param block The function that will be called when this placeholder is requested. It takes the player (if applicable) and the parameters (the part after the placeholder id in the placeholder string) and returns the string to replace the placeholder with.
 * @since 1.7.1
 */
class Placeholder(val id: String, val block: (Player?, String) -> String?) {
    /**
     * Calls the placeholder's block function with the given player and parameters.
      * @param player The player for whom the placeholder is being requested, or null if not applicable.
      * @param params The parameters for this placeholder, which is the part of the placeholder string after the placeholder id (e.g. "player_name" for "%group_player_name%").
      * @return The string to replace the placeholder with, or null if the placeholder should not be replaced.
      * @since 1.7.1
     */
    fun call(player: Player?, params: String): String? {
        return block(player, params)
    }
}