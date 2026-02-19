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

package net.integr.backbone.systems.storage

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test


class ResourceLocationTest {
    @Test
    fun testCreateAndExists() {
        val pool = ResourcePool.fromStorage("test")
        val location = ResourceLocation(pool, "test.txt")

        location.create()
        assert(location.exists())
    }

    @Test
    fun testCreate() {
        val pool = ResourcePool.fromStorage("test")
        val location = ResourceLocation(pool, "test.txt")

        location.create()
        assert(location.exists())
    }

    @Test
    fun testExists() {
        val pool = ResourcePool.fromStorage("test")
        val location = ResourceLocation(pool, "test.txt")

        assert(!location.exists())

        location.create()
        assert(location.exists())
    }

    @Test
    fun testEquals() {
        val pool = ResourcePool.fromStorage("test")
        val location1 = ResourceLocation(pool, "test.txt")
        val location2 = ResourceLocation(pool, "test.txt")

        assert(location1 == location2)
    }

    @AfterEach
    fun tearDown() {
        val pool = ResourcePool.fromStorage("test")
        pool.origin.toFile().deleteRecursively()
    }
}