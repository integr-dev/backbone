package net.integr.backbone.systems.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.applyMeta(block: ItemMeta.() -> Unit) {
    val meta = this.itemMeta
    if (meta != null) {
        block(meta)
        this.itemMeta = meta
    }
}

fun ItemMeta.applyLore(block: (MutableList<String>) -> Unit) {
    val lore = this.lore ?: mutableListOf()
    block(lore)
    this.lore = lore
}