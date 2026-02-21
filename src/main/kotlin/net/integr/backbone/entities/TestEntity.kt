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

package net.integr.backbone.entities


import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.integr.backbone.Backbone
import net.integr.backbone.systems.entity.CustomEntity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf
import java.util.*

object TestEntity : CustomEntity<Zombie>(EntityType.ZOMBIE) {
    override fun prepare(mob: Zombie) {
        val goals = Backbone.SERVER.mobGoals

        goals.removeAllGoals(mob)
        goals.addGoal(mob, 1, LookGoal(mob))
    }

    class LookGoal(val mob: Zombie) : Goal<Zombie> {
        override fun shouldActivate(): Boolean {
            return true
        }

        override fun getKey(): GoalKey<Zombie> {
            return getGoalKey("backbone", "look")
        }

        override fun getTypes(): EnumSet<GoalType> {
            return enumSetOf(GoalType.LOOK)
        }

        override fun tick() {
            val nearestPlayer = Backbone.SERVER.onlinePlayers.minBy { it.location.distance(mob.location) }
            mob.lookAt(nearestPlayer)
        }
    }
}