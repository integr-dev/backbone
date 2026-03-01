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

package net.integr.backbone.systems.persistence

import org.jetbrains.annotations.ApiStatus

/**
 * A collection of keys used for persisting data to various storage mechanisms,
 * primarily [org.bukkit.persistence.PersistentDataContainer] for [org.bukkit.inventory.ItemStack]s
 * and entities. Each key is associated with a unique string identifier.
 *
 * These keys are used internally by Backbone to store metadata about custom items,
 * custom entities, and other system-specific data.
 *
 * @property key The string identifier for the persistence key.
 * @since 1.0.0
 */
@ApiStatus.Internal
enum class PersistenceKeys(val key: String) {
    BACKBONE_CUSTOM_ITEM_UID("bbciuid"), // Backbone Custom Item UID
    BACKBONE_CUSTOM_ITEM_INSTANCE_UID("bbciiuid"), // Backbone Custom Item Instance UID
    BACKBONE_CUSTOM_ITEM_STATE_UID("bbcisuid"), // Backbone Custom Item State UID
    BACKBONE_CUSTOM_ENTITY_UID("bbceuid") // Backbone Custom Entity UID
}