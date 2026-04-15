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

package net.integr.backbone.systems.item.antidupe.strategies

import net.integr.backbone.systems.item.antidupe.AntiDupeStrategy
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent

/**
 * An anti-duplication strategy that monitors item pickup events. When a player picks up an item, this strategy checks if the item has a custom instance UID
 * that matches any item currently in the player's inventory. If a match is found, it flags the player for potential duplication.
 * This strategy helps prevent exploits where players might try to duplicate items by dropping and picking them up.
 *
 * @since 1.9.0
 */
object ItemPickupStrategy : AntiDupeStrategy("ItemPickupStrategy") { //TODO: Field test with actual custom items
    @EventHandler
    fun onPickupItem(event: EntityPickupItemEvent) {
        if (event.entity is Player) {
            val iid = read(event.item.itemStack) ?: return
            val hit = inventoryContains(event.entity as Player, iid)

            if (hit) {
                flag(event.entity as Player, iid)
            }
        }
    }
}