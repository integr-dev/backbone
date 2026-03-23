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

package net.integr.backbone.systems.hotloader.isc

/**
 * Builder for [IscMap] used in inter-script communication.
 *
 * Allows adding key-value pairs and building the final immutable map.
 * @since 1.6.0
 */
class IscMapBuilder {
    private val internalMap = mutableMapOf<String, Any>()

    /**
     * Adds a key-value pair to the map.
     *
     * @param key The key to add.
     * @param value The value to associate with the key.
     * @since 1.6.0
     */
    fun put(key: String, value: Any) {
        internalMap[key] = value
    }

    /**
     * Builds the immutable [IscMap].
     *
     * @return The constructed [IscMap].
     * @since 1.6.0
     */
    fun build(): IscMap {
        return IscMap(internalMap)
    }
}
