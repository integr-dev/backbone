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

import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.event.BackboneEventHandler
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.jetbrains.annotations.ApiStatus

/**
 * Handles GUI interactions and manages open GUIs.
 *
 * This object acts as a central point for managing GUIs, including handling inventory close, click, and interact events,
 * as well as ticking open GUIs.
 *
 * @since 1.0.0
 */
object GuiHandler : Listener {

    /**
     * A map of currently open GUIs, where the key is the player and the value is the [GuiState] of the GUI.
     *
     * @since 1.0.0
     */
    val openInventories: MutableMap<Player, GuiState> = mutableMapOf()

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onCloseInventory(event: InventoryCloseEvent) {
        val playersInv = openInventories[event.player as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            openInventories.remove(event.player as Player)
            playersInv.origin.onClose(event)
        }
    }

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onClickInventory(event: InventoryClickEvent) {
        val playersInv = openInventories[event.whoClicked as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            playersInv.origin.onClick(event)
        }
    }

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onInteractInventory(event: InventoryInteractEvent) {
        val playersInv = openInventories[event.whoClicked as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            playersInv.origin.onInteract(event)
        }
    }

    /**
     * Called by backbone.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @BackboneEventHandler
    fun onTick(event: TickEvent) {
        for ((_, guiState) in openInventories) {
            guiState.origin.onTick(guiState.getInventory())
        }
    }
}