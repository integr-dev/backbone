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