package net.integr.backbone.guis

import net.integr.backbone.Backbone
import net.integr.backbone.systems.gui.Gui
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object TestGui : Gui("Test Gui" , 27) {
    val logger = Backbone.LOGGER.derive("test-gui")

    override fun prepare(inventory: Inventory) {
        inventory.setItem(0, ItemStack(Material.GOLDEN_APPLE))
    }

    override fun tick(inventory: Inventory) {
        val randomSlot = (0 until inventory.size).random()
        inventory.setItem(randomSlot, ItemStack(Material.APPLE))
    }

    override fun clicked(inventory: InventoryClickEvent) {
        logger.info("clicked!")
    }
}