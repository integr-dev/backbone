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

package net.integr.backbone.systems.item.antidupe

import net.integr.backbone.Backbone
import net.integr.backbone.systems.persistence.nbt.NbtHelper
import net.integr.backbone.systems.persistence.nbt.NbtKeys
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * An abstract base class for anti-duplication strategies in the Backbone plugin.
 * Each strategy implements a specific method of detecting and preventing item duplication,
 * such as checking for unique item instance IDs in player inventories.
 *
 * Implementations of this class should define the logic for flagging potential dupes and
 * checking player inventories for items that match the criteria of the strategy.
 *
 * @param name A descriptive name for the anti-dupe strategy, used in logging and alerts when a potential dupe is detected.
 * @since 1.9.0
 */
abstract class AntiDupeStrategy(val name: String) : Listener {
    /**
     * Flags a player for potential duplication based on the provided item instance ID (iid). This method is called when a potential dupe is detected,
     * and it logs a warning message and fires an alert to notify administrators or moderators of the
     * suspicious activity. The alert includes the player's name, the item instance ID, and the name of the strategy that triggered the flag.
     *
     * @param player The player who is being flagged for potential duplication.
     * @param iid The item instance ID that was detected as a potential duplicate in the player's inventory.
     *
     * @since 1.9.0
     */
    fun flag(player: Player, iid: String) {
        AntiDupeHandler.logger.warning("${player.name} flagged dupe check (iid: $iid, strategy: $name)")

        Backbone.fireAlert(Backbone.alertFeedbackFormat
            .formatWarning("${player.name} flagged dupe check ($name)"))
    }

    /**
     * Reads the custom item instance UID from an [ItemStack] using the [NbtHelper]. This method checks the item's persistent data for the specific key associated with
     * the custom item instance UID and returns it as a string. If the item does not
     * have the UID or if the data cannot be read, this method returns null. This is used to identify items that are being checked for duplication.
     *
     * @param item The [ItemStack] from which to read the custom item instance UID.
     * @return The custom item instance UID as a string, or null if it cannot be read.
     * @since 1.9.0
     */
    fun read(item: ItemStack): String? {
        return NbtHelper.read(item, NbtKeys.BACKBONE_CUSTOM_ITEM_INSTANCE_UID.key, PersistentDataType.STRING)
    }

    /**
     * Checks if a player's inventory contains an item with the specified item instance ID (iid). This method iterates through the player's inventory and uses the [read] method to check each item for a matching IID.
     * If any item in the inventory has an IID that matches the provided IID, this method returns true, indicating that the player has an item that could potentially be duplicated. If no matching items are found, it returns false.
     *
     * @param player The player whose inventory is being checked for the specified item instance ID.
     * @param iid The item instance ID to check for in the player's inventory.
     * @return true if the player's inventory contains an item with the specified IID, false otherwise.
     * @since 1.9.0
     */
    fun inventoryContains(player: Player, iid: String): Boolean {
        return player.inventory.any {
            val id = read(it)
            id == iid
        }
    }
}