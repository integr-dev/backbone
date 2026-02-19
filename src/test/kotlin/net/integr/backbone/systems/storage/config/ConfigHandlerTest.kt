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

package net.integr.backbone.systems.storage.config

import net.integr.backbone.systems.storage.ResourcePool
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigHandlerTest {
    @Test
    fun testWriteAndReadState() {
        val pool = ResourcePool.fromConfig("test")
        val location = pool.file("test.yml")
        val configHandler = ConfigHandler(location, TestConfig::class)

        val testConfig = TestConfig("testValue", 123)
        configHandler.writeStateSync(testConfig)

        val readConfig = configHandler.updateAndGetStateSync()

        assertEquals(testConfig.testField, readConfig.testField)
        assertEquals(testConfig.testNumber, readConfig.testNumber)
    }

    @Test
    fun testUpdateAndGetStateSync() {
        val pool = ResourcePool.fromConfig("test")
        val location = pool.file("test.yml")
        val configHandler = ConfigHandler(location, TestConfig::class)

        val initialConfig = TestConfig("initial", 1)
        configHandler.writeStateSync(initialConfig)

        // Manually change the file content to simulate external modification
        location.location.writeText("testField: \"updated\"\ntestNumber: 456\n")

        val updatedConfig = configHandler.updateAndGetStateSync()

        assertEquals("updated", updatedConfig.testField)
        assertEquals(456, updatedConfig.testNumber)
    }

    @Test
    fun testGetState() {
        val pool = ResourcePool.fromConfig("test")
        val location = pool.file("test.yml")
        val configHandler = ConfigHandler(location, TestConfig::class)

        assertNull(configHandler.getState())

        val testConfig = TestConfig("cached", 789)
        configHandler.writeStateSync(testConfig)

        assertEquals(testConfig, configHandler.getState())
    }

    @AfterEach
    fun tearDown() {
        val pool = ResourcePool.fromConfig("test")
        pool.origin.toFile().deleteRecursively()
    }

    data class TestConfig(var testField: String = "", var testNumber: Int = 0)
}