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

package net.integr.backbone.systems.persistence.nbt

import net.integr.backbone.Backbone
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A utility object for interacting with Bukkit's [org.bukkit.persistence.PersistentDataContainer] system.
 *
 * This helper provides simplified methods for reading and writing persistent data to
 * [ItemStack]s and [Entity]s, using Backbone's
 * namespaced keys. It also includes a utility for generating unique IDs.
 *
 * @since 1.0.0
 */
object NbtHelper {
    /**
     * Write a persistent data tag to an [ItemStack].

     * This method safely writes a value of a specified type to the [ItemStack]'s
     * [org.bukkit.persistence.PersistentDataContainer]. If the item has no meta,
     * or if the meta cannot be modified, the operation will silently fail.
     * The key is automatically namespaced by Backbone.
     *
     * @param stack The [ItemStack] to write data to.
     * @param key The string identifier for the data tag (will be namespaced).
     * @param type The [PersistentDataType] defining how the data is stored and retrieved.
     * @param value The value to write.
     * @since 1.0.0
     */
    fun <P : Any, C : Any> write(stack: ItemStack, key: String, type: PersistentDataType<P, C>, value: C) {
        val meta = stack.itemMeta ?: return
        val container = meta.persistentDataContainer

        container.set<P, C>(Backbone.getKey("backbone", key), type, value)

        stack.itemMeta = meta
    }

    /**
     * Read a persistent data tag from an [ItemStack].
     *
     * This method safely reads a value of a specified type from the [ItemStack]'s
     * [org.bukkit.persistence.PersistentDataContainer]. If the item has no meta,
     * or if the data tag does not exist, `null` will be returned.
     * The key is automatically namespaced by Backbone.
     *
     * @param stack The [ItemStack] to read data from.
     * @param key The string identifier for the data tag (will be namespaced).
     * @param type The [PersistentDataType] defining how the data is stored and retrieved.
     * @return The read value, or `null` if not found or readable.
     * @since 1.0.0
     */
    fun <P : Any, C : Any> read(stack: ItemStack, key: String, type: PersistentDataType<P, C>): C? {
        val container = stack.itemMeta?.persistentDataContainer ?: return null

        return container.get(Backbone.getKey("backbone", key), type)
    }

    /**
     * Write a persistent data tag to an [Entity].
     *
     * This method safely writes a value of a specified type to the [Entity]'s
     * [org.bukkit.persistence.PersistentDataContainer]. The key is automatically
     * namespaced by Backbone.
     *
     * @param entity The [Entity] to write data to.
     * @param key The string identifier for the data tag (will be namespaced).
     * @param type The [PersistentDataType] defining how the data is stored and retrieved.
     * @param value The value to write.
     * @since 1.0.0
     */
    fun <P : Any, C : Any> write(entity: Entity, key: String, type: PersistentDataType<P, C>, value: C) {
        val container = entity.persistentDataContainer
        container.set<P, C>(Backbone.getKey("backbone", key), type, value)
    }

    /**
     * Read a persistent data tag from an [Entity].
     *
     * This method safely reads a value of a specified type from the [Entity]'s
     * [org.bukkit.persistence.PersistentDataContainer]. If the data tag does not exist,
     * `null` will be returned. The key is automatically namespaced by Backbone.
     *
     * @param entity The [Entity] to read data from.
     * @param key The string identifier for the data tag (will be namespaced).
     * @param type The [PersistentDataType] defining how the data is stored and retrieved.
     * @return The read value, or `null` if not found or readable.
     * @since 1.0.0
     */
    fun <P : Any, C : Any> read(entity: Entity, key: String, type: PersistentDataType<P, C>): C? {
        val container = entity.persistentDataContainer
        return container.get(Backbone.getKey("backbone", key), type)
    }

    /**
     * Generates a unique ID (UUID) as a hexadecimal string.
     *
     * This method uses Kotlin's experimental `kotlin.uuid.Uuid` to generate a
     * cryptographically strong UUID and converts it to a hexadecimal string representation.
     *
     * @return A unique ID string.
     * @since 1.0.0
     */
    @OptIn(ExperimentalUuidApi::class)
    fun genUid(): String {
        return Uuid.random().toHexString()
    }
}