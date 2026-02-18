package net.integr.backbone.items

import net.integr.backbone.systems.item.CustomItem
import net.integr.backbone.systems.item.CustomItemState
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object TestItem : CustomItem("test_item", true, DefaultState) {
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
            val meta = stack.itemMeta ?: return
            val lore = meta.lore ?: mutableListOf()
            lore += "Default"

            meta.lore = lore
            stack.itemMeta = meta
        }

        override fun interacted(event: PlayerInteractEvent) {
            if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND) {
                event.player.inventory.setItemInMainHand(generate(CooldownState))
            }
        }
    }

    object CooldownState : CustomItemState(Material.GOLDEN_HOE, "cooldown") {
        override fun populate(stack: ItemStack) {
            val meta = stack.itemMeta ?: return
            val lore = meta.lore ?: mutableListOf()
            lore += "Cooldown"

            meta.lore = lore
            stack.itemMeta = meta
        }

        override fun interacted(event: PlayerInteractEvent) {
            if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND) {
                event.player.inventory.setItemInMainHand(generate(DefaultState))
            }
        }
    }
}