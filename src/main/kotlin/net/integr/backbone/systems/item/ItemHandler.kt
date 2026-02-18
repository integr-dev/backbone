package net.integr.backbone.systems.item

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemHandler : Listener {
    val items: MutableMap<String, CustomItem> = mutableMapOf()

    fun register(item: CustomItem) {
        items[item.id] = item
    }

    fun generate(item: String): ItemStack {
        val customItem = items[item] ?: throw IllegalArgumentException("Item not found.")
        return customItem.generate()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item
        if (item == null) return

        val customItemUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING)
        if (customItemUid == null) return

        val customItemStateUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postInteracted(customItemStateUid, event)
    }

    @EventHandler
    fun onPlayerDrop(event: EntityDropItemEvent) {
        val item = event.itemDrop

        val customItemUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING)
        if (customItemUid == null) return

        val customItemStateUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postDropped(customItemStateUid, event)
    }

    fun onPlayerPickup(event: EntityPickupItemEvent) {
        val item = event.item

        val customItemUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING)
        if (customItemUid == null) return

        val customItemStateUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postPickup(customItemStateUid, event)
    }
}