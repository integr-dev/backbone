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

package net.integr.backbone.systems.storage.database

import net.integr.backbone.systems.storage.ResourcePool
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DatabaseConnectionTest {
    @Test
    fun testUseConnection() {
        val pool = ResourcePool.fromStorage("test")
        val dbConnection = pool.database("test.db")

        dbConnection.useConnection { conn, _ ->
            execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")

            preparedStatement("INSERT INTO test_table (name) VALUES (?)") {
                setString(1, "TestName")
                executeUpdate()
            }

            val result = query("SELECT name FROM test_table WHERE id = 1") { resultSet ->
                resultSet.getString("name")
            }

            assertEquals("TestName", result)

            execute("DROP TABLE IF EXISTS test_table")
        }
    }

    @AfterEach
    fun tearDown() {
        val pool = ResourcePool.fromStorage("test")
        pool.origin.toFile().deleteRecursively()
    }
}