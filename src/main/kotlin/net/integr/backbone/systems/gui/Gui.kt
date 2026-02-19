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

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory

abstract class Gui(title: String, size: Int = 27) {
    val initialState = GuiState(this, title, size)

    init {
        prepare(initialState.getInventory())
    }

    fun open(player: Player) {
        player.closeInventory() // Close any existing inventory first
        val playerState = initialState.copy()
        val inv = playerState.getInventory()
        GuiHandler.openInventories[player] = playerState
        player.openInventory(inv)
        open(player, inv)
    }

    open fun prepare(inventory: Inventory) {}
    open fun open(player: Player, inventory: Inventory) {}
    open fun close(inventory: InventoryCloseEvent) {}

    open fun tick(inventory: Inventory) {}
    open fun clicked(inventory: InventoryClickEvent) {}
    open fun interacted(inventory: InventoryInteractEvent) {}
}