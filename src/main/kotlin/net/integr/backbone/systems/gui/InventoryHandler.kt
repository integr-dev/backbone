package net.integr.backbone.systems.gui

import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.event.BackboneEventHandler
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent

object InventoryHandler : Listener {
    val openInventories: MutableMap<Player, GuiState> = mutableMapOf()

    @EventHandler
    fun onCloseInventory(event: InventoryCloseEvent) {
        val playersInv = openInventories[event.player as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            openInventories.remove(event.player as Player)
            playersInv.origin.close(event)
        }
    }

    @EventHandler
    fun onClickInventory(event: InventoryClickEvent) {
        val playersInv = openInventories[event.whoClicked as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            playersInv.origin.clicked(event)
        }
    }

    @EventHandler
    fun onInteractInventory(event: InventoryInteractEvent) {
        val playersInv = openInventories[event.whoClicked as Player] ?: return
        if (playersInv.getInventory() == event.inventory) {
            playersInv.origin.interacted(event)
        }
    }

    @BackboneEventHandler
    fun onTick(event: TickEvent) {
        for ((_, guiState) in openInventories) {
            guiState.origin.tick(guiState.getInventory())
        }
    }
}