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

package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.entity.TextDisplay

/**
 * A renderable that manages a single [TextDisplay] object.
 *
 * This class provides a convenient way to create, spawn, despawn, and update a single
 * [TextDisplay] entity, making it easier to render dynamic text in the world.
 *
 * @since 1.0.0
 */
class TextRenderable : Renderable() {
    private val text = textDisplay()

    /**
     * Updates the [TextDisplay] entity with the given location and applies the provided block of code.

     * @param location The new location for the text display.
     * @param block A lambda function to apply additional properties to the [TextDisplay] entity.
     * @since 1.0.0
     */
    fun update(location: Location, block: TextDisplay.() -> Unit) {
        this.text.entity?.let {
            it.teleport(location)
            it.block()
        }
    }
}