package net.integr.backbone.systems.item

import org.bukkit.Material
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class CustomItemState(val material: Material, val id: String) {
    fun generate(): ItemStack {
        val stack = ItemStack(material)
        populate(stack)
        attach(stack)
        return stack
    }

    private fun attach(stack: ItemStack) {
        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING, id)
    }

    protected open fun populate(stack: ItemStack) {}

    open fun interacted(event: PlayerInteractEvent) {}
    open fun dropped(event: EntityDropItemEvent) {}
    open fun pickup(event: EntityPickupItemEvent) {}
}