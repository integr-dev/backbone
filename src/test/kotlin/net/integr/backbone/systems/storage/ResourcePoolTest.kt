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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResourcePoolTest {
    @Test
    fun testAllocate() {
        val pool = ResourcePool.fromStorage("test")
        val location = pool.allocate("test.txt")

        assertEquals(location.location.name, "test.txt")
    }

    @Test
    fun testCreate() {
        val pool = ResourcePool.fromStorage("test")
        pool.create()

        assert(pool.exists())
    }

    @Test
    fun testExists() {
        val pool = ResourcePool.fromStorage("test")

        assert(!pool.exists())

        pool.create()
        assert(pool.exists())
    }

    @Test
    fun testListFiles() {
        val pool = ResourcePool.fromStorage("test")
        pool.create()

        val file1 = pool.allocate("file1.txt")
        file1.create()
        val file2 = pool.allocate("file2.txt")
        file2.create()

        val files = pool.listFiles()
        assertEquals(2, files.size)
        assertTrue(files.any { it.endsWith("file1.txt") })
        assertTrue(files.any { it.endsWith("file2.txt") })
    }

    @Test
    fun testEquals() {
        val pool1 = ResourcePool.fromStorage("test")
        val pool2 = ResourcePool.fromStorage("test")
        val pool3 = ResourcePool.fromStorage("other")

        assertEquals(pool1, pool2)
        assertNotEquals(pool1, pool3)

        pool1.origin.toFile().deleteRecursively()
        pool2.origin.toFile().deleteRecursively()
        pool3.origin.toFile().deleteRecursively()
    }

    @AfterEach
    fun tearDown() {
        val pool1 = ResourcePool.fromStorage("test")
        val pool2 = ResourcePool.fromStorage("test")
        val pool3 = ResourcePool.fromStorage("other")
        pool1.origin.toFile().deleteRecursively()
        pool2.origin.toFile().deleteRecursively()
        pool3.origin.toFile().deleteRecursively()
    }
}