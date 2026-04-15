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

import net.integr.backbone.Utils
import net.integr.backbone.systems.persistence.nbt.NbtHelper
import net.integr.backbone.systems.persistence.nbt.NbtKeys
import org.bukkit.Material
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a specific state or variation of a [CustomItem].
 *
 * This abstract class provides the foundation for defining different states for a custom item.
 * Each state can have a unique ID, material, and specific behaviors when interacted with,
 * dropped, or picked up. It integrates with Backbone's persistence system to store
 * the state ID on the [ItemStack].
 *
 * @property material The [Material] of the [ItemStack] for this state.
 * @property id The unique identifier for this custom item state. Must be in snake_case.
 * @since 1.0.0
 */
abstract class CustomItemState(val material: Material, val id: String) {
    init {
        if (!Utils.isSnakeCase(id)) throw IllegalArgumentException("ID must be snake_case")
    }

    /**
     * Generate a new [ItemStack] for this custom item state.
     *
     * This method creates an [ItemStack] based on the [material] of this state,
     * attaches the necessary Backbone persistence data (state UID),
     * and then calls [populate] to allow for further customization.
     *
     * @return The newly created and configured [ItemStack].
     * @since 1.0.0
     */
    fun generate(): ItemStack {
        val stack = ItemStack(material)
        populate(stack)
        attach(stack)
        return stack
    }

    /**
     * Attach the necessary Backbone persistence data to an [ItemStack].
     *
     * This private helper method writes the custom item state's UID
     * into the item's [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param stack The [ItemStack] to attach the data to.
     * @since 1.0.0
     */
    private fun attach(stack: ItemStack) {
        NbtHelper.write(stack, NbtKeys.BACKBONE_CUSTOM_ITEM_STATE_UID.key, PersistentDataType.STRING, id)
    }

    /**
     * Called after an [ItemStack] has been generated and its persistence data attached.
     *
     * This method is intended to be overridden by subclasses to perform additional
     * modifications to the [ItemStack], such as setting display names, lore, enchantments,
     * or other custom NBT data specific to this state.
     *
     * @param stack The [ItemStack] that has just been generated and had its persistence data attached.
     * @since 1.0.0
     */
    protected open fun populate(stack: ItemStack) {}

    /**
     * Called when a player interacts with this custom item state.
     *
     * This method is called when a player performs an action (e.g., right-click, left-click)
     * with an instance of a custom item that is in this specific state.
     *
     * @param event The [PlayerInteractEvent] representing the interaction.
     * @since 1.0.0
     */
    open fun onInteract(event: PlayerInteractEvent) {}

    /**
     * Called when an entity drops an instance of this custom item state.
     *
     * This method is called when an entity (e.g., a player, a mob) drops an item stack
     * that is an instance of a custom item in this specific state.
     *
     * @param event The [EntityDropItemEvent] representing the item drop.
     * @since 1.0.0
     */
    open fun onDrop(event: EntityDropItemEvent) {}

    /**
     * Called when an entity picks up an instance of this custom item state.
     *
     * This method is called when an entity (e.g., a player, a mob) picks up an item stack
     * that is an instance of a custom item in this specific state.
     *
     * @param event The [EntityPickupItemEvent] representing the item pickup.
     * @since 1.0.0
     */
    open fun onPickup(event: EntityPickupItemEvent) {}
}