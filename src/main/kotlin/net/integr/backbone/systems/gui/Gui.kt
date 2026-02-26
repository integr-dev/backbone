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

abstract class Gui(title: Component, size: Int = 27) {
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
        onOpen(player, inv)
    }

    protected open fun prepare(inventory: Inventory) {}
    open fun onOpen(player: Player, inventory: Inventory) {}
    open fun onClose(inventory: InventoryCloseEvent) {}

    open fun onTick(inventory: Inventory) {}
    open fun onClick(inventory: InventoryClickEvent) {}
    open fun onInteract(inventory: InventoryInteractEvent) {}
}