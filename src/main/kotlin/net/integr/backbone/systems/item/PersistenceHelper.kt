package net.integr.backbone.systems.item

import net.integr.backbone.Backbone
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PersistenceHelper {
    fun <P, C : Any> write(stack: ItemStack, key: String, type: PersistentDataType<P, C>, value: C) {
        val meta = stack.itemMeta ?: return
        val container = meta.persistentDataContainer

        container.set<P, C>(getKey(key), type, value)

        stack.itemMeta = meta
    }

    fun <P, C : Any> read(stack: ItemStack, key: String, type: PersistentDataType<P, C>): C? {
        val container = stack.itemMeta?.persistentDataContainer ?: return null

        return container.get<P, C>(getKey(key), type)
    }

    fun getKey(key: String): NamespacedKey {
        return NamespacedKey.fromString(key, Backbone.PLUGIN)!!
    }

    @OptIn(ExperimentalUuidApi::class)
    fun genUid(): String {
        return Uuid.random().toHexString()
    }
}