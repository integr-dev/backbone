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

package net.integr.backbone.items

import net.integr.backbone.systems.item.CustomItem
import net.integr.backbone.systems.item.CustomItemState
import net.integr.backbone.systems.item.ItemHandler
import net.integr.backbone.systems.item.applyLore
import net.integr.backbone.systems.item.applyMeta
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object TestItem : CustomItem("test_item", DefaultState) {
    init {
        register(DefaultState)
        register(CooldownState)
    }

    override fun interacted(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND) {
            event.player.sendMessage("Clicked item!")
        }
    }

    object DefaultState : CustomItemState(Material.IRON_HOE, "default") {
        override fun populate(stack: ItemStack) {
            stack.applyMeta {
                applyLore { lore ->
                    lore += "Default"
                }
            }
        }

        override fun interacted(event: PlayerInteractEvent) {
            if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND) {
                val id = ItemHandler.getInstanceId(event.player.inventory.itemInMainHand)
                event.player.inventory.setItemInMainHand(generate(CooldownState, id))
            }
        }
    }

    object CooldownState : CustomItemState(Material.GOLDEN_HOE, "cooldown") {
        override fun populate(stack: ItemStack) {
            stack.applyMeta {
                applyLore { lore ->
                    lore += "Cooldown"
                }
            }
        }

        override fun interacted(event: PlayerInteractEvent) {
            if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND) {
                val id = ItemHandler.getInstanceId(event.player.inventory.itemInMainHand)
                event.player.inventory.setItemInMainHand(generate(DefaultState, id))
            }
        }
    }
}