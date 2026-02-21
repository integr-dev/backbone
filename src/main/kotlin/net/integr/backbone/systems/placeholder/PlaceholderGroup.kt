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

import net.integr.backbone.Backbone
import org.bukkit.entity.Player

class PlaceholderGroup(val version: String, val author: String) {
    companion object {
        val logger = Backbone.LOGGER.derive("placeholder-group")
    }

    private val extensions = mutableListOf<PlaceholderApiExtension>()

    fun create(id: String, block: (Player?, String) -> String): PlaceholderApiExtension {
        val ex = PlaceholderApiExtension(id, author, version, block)
        extensions += ex
        return ex
    }

    fun registerAll() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.register() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    fun unregisterAll() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.unregister() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    fun getById(id: String): PlaceholderApiExtension? {
        return extensions.find { it.id == id }
    }
}