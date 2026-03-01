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

package net.integr.backbone.systems.gui

import net.integr.backbone.Backbone
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * Represents the state of a GUI for a specific player.
 * This class holds the [Inventory] instance and other relevant information for an open GUI,
 * allowing for individual player states of the same GUI definition.
 *
 * @param origin The original [Gui] instance this state is based on.
 * @param title The title of the GUI.
 * @param size The size of the GUI (number of slots).
 * @since 1.0.0
 */
@ApiStatus.Internal
class GuiState(val origin: Gui, val title: Component, val size: Int = 27) {
    private val inventory: Inventory = Backbone.SERVER.createInventory(null, size, title)

    /**
     * Creates a copy of this [GuiState] instance.
     *
     * @return A new [GuiState] instance with the same origin, title, size, and contents.
     * @since 1.0.0
     */
    fun copy(): GuiState {
        val newState = GuiState(origin, title, size)
        newState.setContents(this.getContents())
        return newState
    }

    /**
     * Returns the [Inventory] instance associated with this GUI state.
     *
     * @return The [Inventory] instance.
     * @since 1.0.0
     */
    fun getInventory(): Inventory {
        return inventory
    }

    /**
     * Sets the contents of the inventory.
     *
     * @param contents The array of [ItemStack]s to set as the inventory's contents.
     * @since 1.0.0
     */
    private fun setContents(contents: Array<ItemStack?>) {
        inventory.contents = contents
    }

    /**
     * Returns the contents of the inventory as an array of [ItemStack]s.
     *
     * @return An array of [ItemStack]s representing the inventory's contents.
     * @since 1.0.0
     */
    private fun getContents(): Array<ItemStack?> {
        return inventory.contents
    }
}