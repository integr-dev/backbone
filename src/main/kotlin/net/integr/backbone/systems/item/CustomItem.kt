package net.integr.backbone.systems.item

import org.bukkit.Material
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class CustomItem(val id: String, val attachInstanceId: Boolean, val defaultState: CustomItemState) {
    private val states: MutableMap<String, CustomItemState> = mutableMapOf()

    fun register(state: CustomItemState) {
        states[state.id] = state
    }

    fun generate(state: CustomItemState): ItemStack {
        val stack = state.generate()

        attach(stack)
        populate(stack)
        return stack
    }
    fun generate() = generate(defaultState)


    private fun attach(stack: ItemStack) {
        PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_UID.key, PersistentDataType.STRING, id)

        if (attachInstanceId) {
            PersistenceHelper.write(stack, PersistenceKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING, PersistenceHelper.genUid())
        }
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