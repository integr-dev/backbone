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

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory

/**
 *  * Represents a graphical user interface (GUI) for players.
 *
 * This abstract class provides a framework for creating custom GUIs with inventory-based interactions.
 * Subclasses should implement the [prepare] method to define the GUI's layout.
 *
 * @param title The title of the GUI.
 * @param size The size of the GUI (number of slots), must be a multiple of 9.
 * @since 1.0.0
 */
abstract class Gui(title: Component, size: Int = 27) {

    /**
     * The initial state of the GUI, used as a template for new instances.
     * @since 1.0.0
     */
    private val initialState = GuiState(this, title, size)

    init {
        prepare(initialState.getInventory())
    }

    /**
     *
     * Opens the GUI for the specified player.
     *
     * @param player The player to open the GUI for.
     * @since 1.0.0
     */
    fun open(player: Player) {
        player.closeInventory() // Close any existing inventory first
        val playerState = initialState.copy()
        val inv = playerState.getInventory()
        GuiHandler.openInventories[player] = playerState
        player.openInventory(inv)
        onOpen(player, inv)
    }

    /**
     *
     * Prepares the GUI by setting up its contents.
     *
     * This method is called once when the GUI is initialized. Subclasses should override this method
     * to populate the inventory with items, define their positions, and set up any initial state.
     *
     * @param inventory The [Inventory] instance representing the GUI.
     * @since 1.0.0
     */
    protected open fun prepare(inventory: Inventory) {}

    /**
     *
     * Called when the GUI is opened for a player.
     *
     * @param player The player who opened the GUI.
     * @param inventory The [Inventory] instance representing the GUI.
     * @since 1.0.0
     */
    open fun onOpen(player: Player, inventory: Inventory) {}

    /**
     * Called when the GUI is closed by a player.
     *
     * @param inventory The [InventoryCloseEvent] instance representing the GUI close event.
     * @since 1.0.0
     */
    open fun onClose(inventory: InventoryCloseEvent) {}

    /**
     *
     * Called every tick while the GUI is open.
     *
     * @param inventory The [Inventory] instance representing the GUI.
     * @since 1.0.0
     */
    open fun onTick(inventory: Inventory) {}

    /**
     * Called when a player clicks an item in the GUI.
     *
     * @param inventory The [InventoryClickEvent] instance representing the click event.
     * @since 1.0.0
     */
    open fun onClick(inventory: InventoryClickEvent) {}

    /**
     * Called when a player interacts with the GUI.
     *
     * @param inventory The [InventoryInteractEvent] instance representing the interact event.
     * @since 1.0.0
     */
    open fun onInteract(inventory: InventoryInteractEvent) {}
}