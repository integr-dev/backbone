package net.integr.backbone.systems.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory

abstract class Gui(title: String, size: Int = 27) {
    val initialState: GuiState = GuiState(this, title, size)

    init {
        prepare(initialState.getInventory())
    }

    fun open(player: Player) {
        player.closeInventory() // Close any existing inventory first
        val playerState = initialState.copy()
        val inv = playerState.getInventory()
        InventoryHandler.openInventories[player] = playerState
        player.openInventory(inv)
        open(player, inv)
    }

    abstract fun prepare(inventory: Inventory)
    abstract fun open(player: Player, inventory: Inventory)
    abstract fun close(inventory: InventoryCloseEvent)

    abstract fun tick(inventory: Inventory)
    abstract fun clicked(inventory: InventoryClickEvent)
    abstract fun interacted(inventory: InventoryInteractEvent)



}