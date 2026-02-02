package net.integr.backbone.guis

import net.integr.backbone.systems.gui.Gui
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

// Create a Test Inventory Gui with 27 slots
object TestGui : Gui("Test Gui" , 27) {
    // Prepare is ran once during construction to set up base layouts
    // Use this to for example load fixed content and for example buttons
    override fun prepare(inventory: Inventory) {
        inventory.setItem(0, ItemStack(Material.GOLDEN_APPLE))
    }

    // Ran whenever the inventory is first loaded for a player
    // Use this to dynamically load data
    override fun open(player: Player, inventory: Inventory) {
        // Opened!
    }

    // Called when the inventory is closed
    // Do not use this to re-open inventories!
    // If you want to re-open another inventory wrap your
    // .open(<player>) call with Backbone.dispatchMain {}
    // To schedule the operation for the next tick
    override fun close(inventory: InventoryCloseEvent) {
        // Closed!
    }

    // Runs every game tick
    // Used for animations and other logic
    override fun tick(inventory: Inventory) {
        val randomSlot = (0 until inventory.size).random()
        inventory.setItem(randomSlot, ItemStack(Material.APPLE))
    }

    // Runs when a slot is clicked in the inventory
    override fun clicked(inventory: InventoryClickEvent) {
        // Clicked!
    }

    // Runs on any interaction (including clicks)
    override fun interacted(inventory: InventoryInteractEvent) {
        // Interacted!
    }
}