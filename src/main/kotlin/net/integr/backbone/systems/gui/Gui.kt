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