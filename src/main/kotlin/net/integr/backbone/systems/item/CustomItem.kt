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

import org.bukkit.Material
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class CustomItem(val id: String, val defaultState: CustomItemState) {
    private val states: MutableMap<String, CustomItemState> = mutableMapOf()

    fun register(state: CustomItemState) {
        states[state.id] = state
    }

    fun generate(state: CustomItemState, instanceId: String = PersistenceHelper.genUid()): ItemStack {
        val stack = state.generate()

        attach(stack, instanceId)
        populate(stack)
        return stack
    }

    fun generate() = generate(defaultState)


    private fun attach(stack: ItemStack, instanceId: String) {
        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING, id)
        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING, instanceId)
    }

    protected open fun populate(stack: ItemStack) {}

    fun postInteracted(customItemStateUid: String?, event: PlayerInteractEvent) {
        interacted(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.interacted(event)
    }

    fun postDropped(customItemStateUid: String?, event: EntityDropItemEvent) {
        dropped(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.dropped(event)
    }

    fun postPickup(customItemStateUid: String?, event: EntityPickupItemEvent) {
        pickup(event)

        if (customItemStateUid == null) return
        val state = states[customItemStateUid] ?: return
        state.pickup(event)
    }

    open fun interacted(event: PlayerInteractEvent) {}
    open fun dropped(event: EntityDropItemEvent) {}
    open fun pickup(event: EntityPickupItemEvent) {}
}