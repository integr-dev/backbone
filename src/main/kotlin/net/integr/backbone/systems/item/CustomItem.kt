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
import net.integr.backbone.systems.persistence.PersistenceHelper
import net.integr.backbone.systems.persistence.PersistenceKeys
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a custom item managed by the Backbone system.
 *
 * This abstract class provides the foundation for defining custom items with unique IDs,
 * default states, and lifecycle methods for handling interactions, drops, and pickups.
 * It integrates with Backbone's persistence system to manage item metadata and states.
 *
 * Custom items can have multiple [CustomItemState]s, allowing for variations in their
 * appearance, behavior, or properties. The `defaultState` is used when generating
 * an item without specifying a particular state.
 *
 * @property id The unique identifier for this custom item. Must be in snake_case.
 * @property defaultState The default [CustomItemState] for this custom item.
 * @since 1.0.0
 */
abstract class CustomItem(val id: String, val defaultState: CustomItemState) {
    init {
        if (!Utils.isSnakeCase(id)) throw IllegalArgumentException("ID must be snake_case")
    }

    private val states: MutableMap<String, CustomItemState> = mutableMapOf()

    /**
     * Register a new [CustomItemState] for this custom item.
     *
     * @param state The [CustomItemState] to register.
     * @since 1.0.0
     */
    fun register(state: CustomItemState) {
        states[state.id] = state
    }

    /**
     * Generate a new [ItemStack] for this custom item with a specific state and instance ID.
     *
     * This method creates an [ItemStack] based on the provided [CustomItemState],
     * attaches the necessary Backbone persistence data (item UID, instance UID),
     * and then calls [populate] to allow for further customization.
     *
     * @param state The [CustomItemState] to use for generating the item.
     * @param instanceId A unique identifier for this specific instance of the item.
     *                   If not provided, a new UID will be generated.
     * @return The newly created and configured [ItemStack].
     * @throws IllegalArgumentException if the provided `instanceId` is not in snake_case.
     * @since 1.0.0
     */
    fun generate(state: CustomItemState, instanceId: String = PersistenceHelper.genUid()): ItemStack {
        if (!Utils.isUid(instanceId)) throw IllegalArgumentException("Instance ID must be snake_case")

        val stack = state.generate()

        attach(stack, instanceId)
        populate(stack)
        return stack
    }

    /**
     * Generate a new [ItemStack] for this custom item using its default state.
     *
     * This method is a convenience function that calls [generate] with the [defaultState]
     * of this custom item.
     *
     * @return The newly created and configured [ItemStack].
     * @since 1.0.0
     */
    fun generate() = generate(defaultState)

    /**
     * Attach the necessary Backbone persistence data to an [ItemStack].

     * This private helper method writes the custom item's UID and the specific instance's UID
     * into the item's [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param stack The [ItemStack] to attach the data to.
     * @param instanceId The unique instance ID for this specific item stack.
     * @throws IllegalArgumentException if the provided `instanceId` is not a valid uid.
     * @since 1.0.0
     */
    private fun attach(stack: ItemStack, instanceId: String) {
        if (!Utils.isUid(instanceId)) throw IllegalArgumentException("Instance ID must be a valid uid")

        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING, id)
        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING, instanceId)
    }

    /**
     * Called after an [ItemStack] has been generated and its persistence data attached.

     * This method is intended to be overridden by subclasses to perform additional
     * modifications to the [ItemStack], such as setting display names, lore, enchantments,
     * or other custom NBT data.
     *
     * @param stack The [ItemStack] that has just been generated and had its persistence data attached.
     * @since 1.0.0
     */
    protected open fun populate(stack: ItemStack) {}

    /**
     * Called by backbone.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun postInteract(customItemStateUid: String?, event: PlayerInteractEvent) {
        onInteract(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.onInteract(event)
    }

    /**
     * Called by backbone.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun postDrop(customItemStateUid: String?, event: EntityDropItemEvent) {
        onDrop(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.onDrop(event)
    }

    /**
     * Called by backbone.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun postPickup(customItemStateUid: String?, event: EntityPickupItemEvent) {
        onPickup(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.onPickup(event)
    }

    /**
     * Called when a player interacts with this custom item.
     *
     * This method is called when a player performs an action (e.g., right-click, left-click)
     * with an instance of this custom item.
     *
     * @param event The [PlayerInteractEvent] representing the interaction.
     * @since 1.0.0
     */
    protected open fun onInteract(event: PlayerInteractEvent) {}

    /**
     * Called when an entity drops an instance of this custom item.
     *
     * This method is called when an entity (e.g., a player, a mob) drops an item stack
     * that is an instance of this custom item.
     *
     * @param event The [EntityDropItemEvent] representing the item drop.
     * @since 1.0.0
     */
    protected open fun onDrop(event: EntityDropItemEvent) {}

    /**
     * Called when an entity picks up an instance of this custom item.
     *
     * This method is called when an entity (e.g., a player, a mob) picks up an item stack
     * that is an instance of this custom item.
     *
     * @param event The [EntityPickupItemEvent] representing the item pickup.
     * @since 1.0.0
     */
    protected open fun onPickup(event: EntityPickupItemEvent) {}
}