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

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * Represents an Extension for use with PlaceholderAPI.
 *
 * This class extends PlaceholderAPI's `PlaceholderExpansion` to allow for custom
 * placeholder logic to be defined. It takes an ID, author, version, and a lambda
 * function that resolves the placeholder request.
 *
 * @property id The unique identifier for this placeholder (e.g., "myplugin").
 * @property pAuthor The author of this placeholder expansion.
 * @property pVersion The version of this placeholder expansion.
 * @property block A lambda function that takes a [Player] and a string identifier
 *              (representing the specific placeholder requested, e.g., "name" for `%myplugin_name%`)
 *              and returns the resolved string value.
 * @since 1.0.0
 */
class PlaceholderApiExtension(val id: String, val pAuthor: String, val pVersion: String, val block: (Player?, String) -> String) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return id
    }

    override fun getAuthor(): String {
        return pAuthor
    }

    override fun getVersion(): String {
        return pVersion
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        return block(player, params)
    }
}