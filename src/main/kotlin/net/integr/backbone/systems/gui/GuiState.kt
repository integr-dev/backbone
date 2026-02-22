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

package net.integr.backbone.systems.gui

import net.integr.backbone.Backbone
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GuiState(val origin: Gui, val title: Component, val size: Int = 27) {
    private val inventory: Inventory = Backbone.SERVER.createInventory(null, size, title)

    fun copy(): GuiState {
        val newState = GuiState(origin, title, size)
        newState.setContents(this.getContents())
        return newState
    }

    fun getInventory(): Inventory {
        return inventory
    }

    private fun setContents(contents: Array<ItemStack?>) {
        inventory.contents = contents
    }

    private fun getContents(): Array<ItemStack?> {
        return inventory.contents
    }
}