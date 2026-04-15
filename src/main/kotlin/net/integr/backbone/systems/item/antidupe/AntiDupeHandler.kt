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
import net.integr.backbone.systems.item.antidupe.strategies.ItemPickupStrategy

/**
 * The central handler for the anti-duplication system in Backbone.
 * This object is responsible for initializing and managing various anti-dupe strategies that monitor player actions to detect and prevent item duplication exploits.
 *
 * The system never takes direct action to remove items from players, but instead focuses on detecting suspicious behavior and logging it for server administrators to review.
 * This allows server staff to investigate potential exploits without risking false positives that could negatively impact player experience.
 *
 * @since 1.9.0
 */
object AntiDupeHandler {
    val logger = Backbone.LOGGER.derive("anti-dupe")

    /**
     * A list of all active anti-duplication strategies. Each strategy is responsible for monitoring specific player actions (e.g., item pickups, drops, inventory interactions) and flagging potential dupes when suspicious behavior is detected.
     * New strategies can be added to this list as needed to expand the coverage of the anti-dupe system. Each strategy should implement the logic for detecting potential duplication based on its specific monitoring focus.
     *
     * @since 1.9.0
     */
    private val strategies = listOf(
        ItemPickupStrategy
    )

    /**
     * Initializes the anti-duplication system by registering all defined strategies as event listeners. This method should be called during the plugin's startup sequence.
     *
     * If the anti-dupe system is disabled in the configuration, this method will log a warning and skip initialization to avoid unnecessary overhead.
     *
     * @since 1.9.0
     */
    fun initialize() {
        if (!Backbone.CONFIG_STATE.useAntiDupe) {
            logger.warning("Anti-dupe system is disabled in the config, skipping initialization.")
            return
        }

        strategies.forEach {
            Backbone.registerListener(it)
        }
    }
}