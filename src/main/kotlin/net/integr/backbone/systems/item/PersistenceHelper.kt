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

import net.integr.backbone.Backbone
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PersistenceHelper {
    fun <P : Any, C : Any> write(stack: ItemStack, key: String, type: PersistentDataType<P, C>, value: C) {
        val meta = stack.itemMeta ?: return
        val container = meta.persistentDataContainer

        container.set<P, C>(Backbone.getKey("backbone", key), type, value)

        stack.itemMeta = meta
    }

    fun <P : Any, C : Any> read(stack: ItemStack, key: String, type: PersistentDataType<P, C>): C? {
        val container = stack.itemMeta?.persistentDataContainer ?: return null

        return container.get(Backbone.getKey("backbone", key), type)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun genUid(): String {
        return Uuid.random().toHexString()
    }
}