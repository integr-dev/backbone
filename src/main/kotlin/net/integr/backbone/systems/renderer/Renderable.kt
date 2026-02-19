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

import net.integr.backbone.systems.renderer.obj.BlockDisplayObject
import net.integr.backbone.systems.renderer.obj.EntityBackedObject
import net.integr.backbone.systems.renderer.obj.ItemDisplayObject
import net.integr.backbone.systems.renderer.obj.TextDisplayObject
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay

abstract class Renderable {
    protected val objects = mutableListOf<EntityBackedObject<*>>()

    fun <T : Entity> obj(obj: EntityBackedObject<T>): EntityBackedObject<T> {
        objects += obj
        return obj
    }

    fun blockDisplay(): EntityBackedObject<BlockDisplay> {
        val e = BlockDisplayObject()
        objects += e
        return e
    }

    fun textDisplay(): EntityBackedObject<TextDisplay> {
        val e = TextDisplayObject()
        objects += e
        return e
    }

    fun itemDisplay(): EntityBackedObject<ItemDisplay> {
        val e = ItemDisplayObject()
        objects += e
        return e
    }

    open fun spawn(world: World, location: Location) {
        for (o in objects) {
            o.spawn(world, location)
        }
    }

    open fun despawn() {
        for (o in objects) {
            o.despawn()
        }
    }

    fun spawnOrRespawn(world: World, location: Location) {
        despawn()
        spawn(world, location)
    }

    open fun exists(): Boolean {
        return objects.all { it.exists() }
    }
}