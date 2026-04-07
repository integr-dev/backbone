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
import net.integr.backbone.Backbone
import org.bukkit.entity.Player

/**
 * A group of placeholders that can be registered together.
 * This is useful for plugins that want to register multiple placeholders under the same identifier.
 * @param pId The identifier for this placeholder group. This is the prefix that will be used for all placeholders in this group.
 * @param pAuthor The author of this placeholder group.
 * @param pVersion The version of this placeholder group.
 * @since 1.0.0
 */
class PlaceholderGroup(val pId: String, val pAuthor: String, val pVersion: String): PlaceholderExpansion() {
    companion object {
        private val logger = Backbone.LOGGER.derive("placeholder-group")
    }

    private val placeholders = mutableMapOf<String, Placeholder>()

    override fun getIdentifier(): String {
        return pId
    }

    override fun getAuthor(): String {
        return pAuthor
    }

    override fun getVersion(): String {
        return pVersion
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val placeholder = PlaceholderHelper.match(placeholders.keys, params)
        val placeholderObj = placeholders[placeholder]
        if (placeholder != null && placeholderObj != null) {
            return placeholderObj.call(player, params)
        }

        return null
    }

    /**
     * Adds a placeholder to this group.
     * @param id The identifier for this placeholder. This is the part that comes after the group identifier in the placeholder string. For example, if the full placeholder is %mygroup_hello%, and the group identifier is "mygroup", then the placeholder identifier would be "hello".
     * @param block The block of code that will be executed when this placeholder is requested.
     * @since 1.7.1
     */
    fun add(id: String, block: (Player?, String) -> String) {
        placeholders[id] = Placeholder(id, block)
    }

    /**
     * Registers this placeholder group with PlaceholderAPI.
     * @since 1.7.1
     */
    fun registerPlaceholders() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {

            super.register()
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    /**
     * Unregisters this placeholder group from PlaceholderAPI.
     * @since 1.7.1
     */
    fun unregisterPlaceholders() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            super.unregister()
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }
}