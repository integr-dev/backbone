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
 * Immutable map for inter-script communication data.
 *
 * Provides type-safe access to values and a builder for construction.
 * @since 1.6.0
 */
class IscMap(private val internalMap: Map<String, Any>) : Map<String, Any> {

    override val size: Int
        get() = internalMap.size
    override val keys: Set<String>
        get() = internalMap.keys
    override val values: Collection<Any>
        get() = internalMap.values
    override val entries: Set<Map.Entry<String, Any>>
        get() = internalMap.entries

    override fun isEmpty(): Boolean = internalMap.isEmpty()

    override fun containsKey(key: String): Boolean = internalMap.containsKey(key)

    override fun containsValue(value: Any): Boolean = internalMap.containsValue(value)

    override operator fun get(key: String): Any? = internalMap[key]

    /**
     * Retrieves a value from the map and casts it to the requested type.
     *
     * @param key The key to look up.
     * @return The value cast to the requested type.
     * @throws ClassCastException if the value is not of the expected type.
     * @since 1.6.0
     */
    fun <T> pull(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return get(key) as T
    }

    companion object {
        /**
         * Builds an [IscMap] using the provided builder block.
         *
         * @param block The builder block.
         * @return The constructed [IscMap].
         * @since 1.6.0
         */
        fun builder(block: IscMapBuilder.() -> Unit): IscMap {
            val builder = IscMapBuilder()
            block(builder)
            return builder.build()
        }
    }
}