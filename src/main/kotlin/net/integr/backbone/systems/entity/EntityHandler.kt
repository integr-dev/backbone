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

package net.integr.backbone.systems.entity

import net.integr.backbone.Backbone
import net.integr.backbone.systems.persistence.PersistenceHelper
import net.integr.backbone.systems.persistence.PersistenceKeys
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.set

object EntityHandler : Listener {
    private val logger = Backbone.LOGGER.derive("entity-handler")
    val entities: MutableMap<String, CustomEntity<*>> = mutableMapOf()

    fun <T : Mob> register(entity: CustomEntity<T>) {
        entities[entity.id] = entity
    }

    fun spawn(entity: String, location: Location, world: World): Entity {
        val customEntity = entities[entity] ?: throw IllegalArgumentException("Entity not found.")
        return customEntity.spawn(location, world)
    }

    @EventHandler
    fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val id = PersistenceHelper.read(entity, PersistenceKeys.BACKBONE_CUSTOM_ENTITY_UID.key, PersistentDataType.STRING)
            if (id != null) {
                logger.info("Re-creating goals for entity: ${entity.entityId} is '$id' at ${entity.location}")
                val customEntity = entities[id] ?: throw IllegalArgumentException("Entity not found.")
                customEntity.recreateGoals(entity as Mob)
            }
        }
    }
}