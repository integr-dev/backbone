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

import java.io.File

/**
 * Represents a specific resource location within a `ResourcePool`.
 *
 * @param pool The `ResourcePool` this location belongs to.
 * @param id The identifier for this resource location.
 *
 * @since 1.0.0
 */
class ResourceLocation(val pool: ResourcePool, id: String) {
    /**
     * The backing file for this resource location.
     *
     * @since 1.0.0
     */
    val location: File = pool.location.resolve(id).toFile()

    /**
     * Creates the file for this resource location if it doesn't already exist.
     *
     * @return `true` if the file was created, `false` if it already existed.
     *
     * @since 1.0.0
     */
    fun create(): Boolean {
        pool.create()
        return location.createNewFile()
    }

    /**
     * Checks if the file for this resource location exists.
     *
     * @return `true` if the file exists, `false` otherwise.
     *
     * @since 1.0.0
     */
    fun exists(): Boolean {
        return location.exists()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceLocation

        if (pool != other.pool) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pool.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}