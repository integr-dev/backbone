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