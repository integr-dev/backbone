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
import com.destroystokyo.paper.entity.ai.GoalType
import net.integr.backbone.Backbone
import net.integr.backbone.systems.entity.CustomEntity
import net.integr.backbone.systems.text.component
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import java.awt.Color
import java.util.EnumSet

object JumperEntity : CustomEntity<Zombie>("jumper", EntityType.ZOMBIE) {
    override fun prepare(mob: Zombie) {
        mob.customName(component {
            append("Jumper") {
                color(Color.GREEN)
            }
        })
    }

    override fun setupGoals(mob: Zombie) {
        Backbone.SERVER.mobGoals.removeAllGoals(mob)
        Backbone.SERVER.mobGoals.addGoal(mob, 1, JumpGoal(mob))
    }

    class JumpGoal(val mob: Zombie) : Goal<Zombie> {
        override fun shouldActivate(): Boolean = true

        override fun getKey() = getGoalKey<Zombie>("backbone", "look")
        override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE)

        var count = 0

        override fun tick() {
            count++

            if (count % 20 == 0) {
                mob.isJumping = !mob.isJumping
                count = 0
            }
        }
    }
}