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

    fun replicate(item: ItemStack): ItemStack? {
        val customItemUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING)
        if (customItemUid == null) return null
        val customItem = items[customItemUid] ?: throw IllegalArgumentException("Item not found.")

        return customItem.generate()
    }

    fun readTags(item: ItemStack): Map<String, String> {
        val customItemUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING)
        val customItemStateUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)
        val customItemStateInstanceUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING)

        val map = mutableMapOf<String, String>()

        if (customItemUid != null) map[PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key] = customItemUid
        if (customItemStateUid != null) map[PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key] = customItemStateUid
        if (customItemStateInstanceUid != null) map[PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key] = customItemStateInstanceUid

        return map
    }

    fun getInstanceId(item: ItemStack): String {
        return PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING) ?: PersistenceHelper.genUid()
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