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

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Extension function for [ItemStack] to apply modifications to its [ItemMeta].
 *
 * This function provides a convenient way to modify the [ItemMeta] of an [ItemStack]
 * by applying a block of code directly to the item's meta. If the item has no meta,
 * the block will not be executed.
 *
 * Example usage:
 * ```kotlin
 * val itemStack = ItemStack(Material.DIAMOND_SWORD)
 * itemStack.applyMeta {
 *     displayName(Component.text("Legendary Sword"))
 *     isUnbreakable = true
 * }
 * ```
 *
 * @param block A lambda function that receives the [ItemMeta] as its receiver,
 *              allowing direct modification of its properties.
 * @since 1.0.0
 */
fun ItemStack.applyMeta(block: ItemMeta.() -> Unit) {
    val meta = this.itemMeta
    if (meta != null) {
        block(meta)
        this.itemMeta = meta
    }
}

/**
 * Extension function for [ItemMeta] to apply modifications to its lore.
 *
 * This function provides a convenient way to modify the lore of an [ItemMeta]
 * by applying a block of code directly to the item's lore. If the item has no lore,
 * an empty mutable list will be provided to the block.
 *
 * Example usage:
 * ```kotlin
 * itemStack.applyMeta {
 *     applyLore { loreList ->
 *         loreList.add(Component.text("A powerful blade."))
 *         loreList.add(Component.text("Forged in fire."))
 *     }
 * }
 * ```
 *
 * @param block A lambda function that receives a [MutableList] of [Component]s
 *              representing the item's lore, allowing direct modification of its contents.
 * @since 1.0.0
 */
fun ItemMeta.applyLore(block: (MutableList<Component>) -> Unit) {
    val lore = this.lore() ?: mutableListOf()
    block(lore)
    this.lore(lore)
}