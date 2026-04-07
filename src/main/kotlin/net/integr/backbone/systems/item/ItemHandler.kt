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

import net.integr.backbone.systems.persistence.PersistenceHelper
import net.integr.backbone.systems.persistence.PersistenceKeys
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.ApiStatus

/**
 * Handles the registration and management of [CustomItem]s and their interactions.
 *
 * This object acts as a central registry for all custom items defined within the Backbone system.
 * It provides methods for registering new custom items, generating instances of them,
 * and handling various item-related events (interact, drop, pickup).
 *
 * It also integrates with the persistence system to read and write custom item data
 * to [ItemStack]s.
 *
 * @since 1.0.0
 */
object ItemHandler : Listener {
    /**
     * A map storing all registered [CustomItem]s, keyed by their unique ID.
     *
     * @since 1.0.0
     */
    val items: MutableMap<String, CustomItem> = mutableMapOf()

    /**
     * Registers a new [CustomItem] to the system.
     *
     * @param item The [CustomItem] to register.
     * @since 1.0.0
     */
    fun register(item: CustomItem) {
        items[item.id] = item
    }

    /**
     * Unregisters a [CustomItem] from the system.
     *
     * @param item The [CustomItem] to register.
     * @since 1.0.0
     */
    fun unregister(item: CustomItem) {
        items.remove(item.id)
    }

    /**
     * Creates a new [ItemStack] for a registered custom item using its default state.
     *
     * @param item The ID of the custom item to generate.
     * @return The newly created [ItemStack].
     * @throws IllegalArgumentException if the item with the given ID is not found.
     * @since 1.0.0
     */
    fun generate(item: String): ItemStack {
        val customItem = items[item] ?: throw IllegalArgumentException("Item not found.")
        return customItem.generate()
    }

    /**
     * Replicates an existing custom item stack, generating a new instance of the same item.
     *
     * This method reads the custom item's UID from the provided [ItemStack] and then
     * generates a new [ItemStack] using the original custom item's default state.
     * The new item will have a new instance UID.
     *
     * @param item The [ItemStack] to replicate.
     * @return A new [ItemStack] representing a replicated instance of the custom item,
     *         or `null` if the provided item is not a custom item.
     * @throws IllegalArgumentException if the custom item with the read UID is not found.
     * @since 1.0.0

     */
    fun replicate(item: ItemStack): ItemStack? {
        val customItemUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING) ?: return null
        val customItem = items[customItemUid] ?: throw IllegalArgumentException("Item not found.")

        return customItem.generate()
    }

    /**
     *
     * Reads and returns a map of all Backbone-related NBT tags from the given [ItemStack].
     *
     * This method extracts the custom item UID, custom item state UID, and custom item instance UID
     * from the item's [org.bukkit.persistence.PersistentDataContainer] and returns them as a map.
     *
     * @param item The [ItemStack] to read tags from.
     * @return A [Map] where keys are the tag names (e.g., "backbone:custom_item_uid") and values are their string representations.
     * @since 1.0.0
     */
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

    /**
     * Reads the instance ID from the given [ItemStack].
     *
     * If the item does not have a custom item instance UID, a new one will be generated.
     *
     * @param item The [ItemStack] to read the instance ID from.
     * @return The instance ID of the custom item.
     * @since 1.0.0
     */
    fun getInstanceId(item: ItemStack): String {
        return PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING) ?: PersistenceHelper.genUid()
    }

    /**
     * Called by bukkit.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return

        val customItemUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING) ?: return

        val customItemStateUid = PersistenceHelper.read(item, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postInteract(customItemStateUid, event)
    }

    /**
     * Called by bukkit.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onPlayerDrop(event: EntityDropItemEvent) {
        val item = event.itemDrop

        val customItemUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING) ?: return

        val customItemStateUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postDrop(customItemStateUid, event)
    }

    /**
     * Called by bukkit.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onPlayerPickup(event: EntityPickupItemEvent) {
        val item = event.item

        val customItemUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING) ?: return

        val customItemStateUid = PersistenceHelper.read(item.itemStack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING)

        val customItem = items[customItemUid] ?: return
        customItem.postPickup(customItemStateUid, event)
    }
}