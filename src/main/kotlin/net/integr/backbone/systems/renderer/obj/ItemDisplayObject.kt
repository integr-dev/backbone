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

package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay

/**
 * A concrete implementation of [EntityBackedObject] for managing a single [ItemDisplay] entity.
 *
 * This class provides the specific spawning logic for an [ItemDisplay] entity, allowing it to be
 * created in a given [World] at a specified [Location].
 * @since 1.0.0
 */
class ItemDisplayObject : EntityBackedObject<ItemDisplay>() {
    override fun spawn(world: World, location: Location) {
        entity = world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    }
}