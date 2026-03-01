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

/**
 * Group multiple placeholders together.  *
 * This class allows for the creation and management of multiple [PlaceholderApiExtension] instances
 * under a common version and author. It simplifies the registration and unregistration process
 * for groups of placeholders, especially useful for scripts or modules that provide several
 * related placeholders.
 *
 * @property version The version string associated with this group of placeholders.
 * @property author The author string associated with this group of placeholders.
 * @since 1.0.0
 */
class PlaceholderGroup(val version: String, val author: String) {
    companion object {
        private val logger = Backbone.LOGGER.derive("placeholder-group")
    }

    private val extensions = mutableListOf<PlaceholderApiExtension>()

    /**
     * Creates a new [PlaceholderApiExtension] within this group.
     *
     * This method instantiates a [PlaceholderApiExtension] with the provided ID,
     * using the [author] and [version] defined for this [PlaceholderGroup].
     * The `block` lambda defines the logic for resolving the placeholder.
     * The created extension is automatically added to this group's internal list.
     *
     * @param id The unique identifier for the placeholder (e.g., "my_placeholder").
     * @param block A lambda function that takes a [Player] and a string identifier
     *              (representing the specific placeholder requested, e.g., "name" for `%myplugin_name%`)
     *              and returns the resolved string value.
     * @return The newly created [PlaceholderApiExtension] instance.
     * @since 1.0.0
     */
    fun create(id: String, block: (Player?, String) -> String): PlaceholderApiExtension {
        val ex = PlaceholderApiExtension(id, author, version, block)
        extensions += ex
        return ex
    }

    /**
     * Registers all [PlaceholderApiExtension] instances created within this group with PlaceholderAPI.
     *
     * If PlaceholderAPI is installed and enabled, each extension in this group will be registered.
     * If PlaceholderAPI is not available, a warning will be logged, and no extensions will be registered.
     *
     * @since 1.0.0
     */
    fun registerAll() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.register() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    /**
     * Unregisters all [PlaceholderApiExtension] instances created within this group from PlaceholderAPI.
     *
     * If PlaceholderAPI is installed and enabled, each extension in this group will be unregistered.
     * If PlaceholderAPI is not available, a warning will be logged, and no extensions will be unregistered.
     *
     * @since 1.0.0
     */
    fun unregisterAll() {
        if (Backbone.SERVER.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.unregister() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    /**
     * Finds a [PlaceholderApiExtension] by its ID from this group.
     *
     * @param id The ID of the placeholder to retrieve.
     * @return The [PlaceholderApiExtension] with the given ID, or `null` if not found.
     * @since 1.0.0
     */
    fun getById(id: String): PlaceholderApiExtension? {
        return extensions.find { it.id == id }
    }
}