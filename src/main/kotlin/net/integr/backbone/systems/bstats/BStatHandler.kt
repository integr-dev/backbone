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

package net.integr.backbone.systems.bstats

import net.integr.backbone.Backbone
import org.bstats.bukkit.Metrics
import org.jetbrains.annotations.ApiStatus

/**
 * Handles the bStats metrics for Backbone.
 * @since 1.1.0
 */
@ApiStatus.Internal
object BStatHandler {
    private val logger = Backbone.LOGGER.derive("bstats")

    /**
     * Plugin ID taken from the bStats dashboard.
     * @since 1.1.0
     */
    private const val PLUGIN_ID = 29810

    /**
     * The current metrics instance.
     * @since 1.1.0
     */
    private var metrics: Metrics? = null

    /**
     * Initializes the bStats metrics with backbones information.
     * @since 1.1.0
     */
    fun init() {
        logger.info("Initializing bStats...")
        metrics = Metrics(Backbone.PLUGIN, PLUGIN_ID)
    }
}