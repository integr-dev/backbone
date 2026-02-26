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

import com.destroystokyo.paper.entity.ai.GoalKey
import net.integr.backbone.Backbone
import net.integr.backbone.Utils
import net.integr.backbone.systems.persistence.PersistenceHelper
import net.integr.backbone.systems.persistence.PersistenceKeys
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType

abstract class CustomEntity<T : Mob>(val id: String, val type: EntityType, val stopDespawning: Boolean = true) {
    init {
        if (!Utils.isSnakeCase(id)) throw IllegalArgumentException("ID must be snake_case")
    }

    protected abstract fun prepare(mob: T)
    abstract fun setupGoals(mob: T)

    fun recreateGoals(mob: Mob) {
        @Suppress("UNCHECKED_CAST")
        setupGoals(mob as T)
    }

    fun spawn(location: Location, world: World): T {
        val e = world.spawnEntity(location, type)
        @Suppress("UNCHECKED_CAST")
        prepare(e as T)
        setupGoals(e)

        if (stopDespawning) e.removeWhenFarAway = false

        // Tag the entity with the custom entity ID
        PersistenceHelper.write(e, PersistenceKeys.BACKBONE_CUSTOM_ENTITY_UID.key, PersistentDataType.STRING, id)
        return e
    }

    inline fun <reified T : Mob> getGoalKey(namespace: String, key: String): GoalKey<T> {
        if (!Utils.isSnakeCase(namespace)) throw IllegalArgumentException("Namespace must be snake_case")
        if (!Utils.isSnakeCase(key)) throw IllegalArgumentException("Key must be snake_case")
        return GoalKey.of(T::class.java, Backbone.getKey(namespace, key))
    }
}

