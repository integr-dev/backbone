package net.integr.backbone.systems.gui

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GuiState(val origin: Gui, val title: String, val size: Int = 27) {
    private val inventory: Inventory = Bukkit.createInventory(null, size, title)

    fun copy(): GuiState {
        val newState = GuiState(origin, title, size)
        newState.setContents(this.getContents())
        return newState
    }

    fun getInventory(): Inventory {
        return inventory
    }

    private fun setContents(contents: Array<ItemStack>) {
        inventory.contents = contents
    }

    private fun getContents(): Array<ItemStack> {
        return inventory.contents
    }
}