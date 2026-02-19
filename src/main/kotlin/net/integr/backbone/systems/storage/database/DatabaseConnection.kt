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

import net.integr.backbone.systems.storage.ResourceLocation
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Savepoint
import java.util.concurrent.atomic.AtomicInteger

class DatabaseConnection(db: ResourceLocation) : AutoCloseable {
    private val jdbcUrl = "jdbc:sqlite:" + db.location.absolutePath
    private var connection: Connection? = null

    private val useCount = AtomicInteger(0)
    private val lock = Any()

    fun getOrConnect(): Connection = synchronized(lock) {
        if (connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(jdbcUrl)
        }

        useCount.incrementAndGet()
        return connection!!
    }

    fun releaseOrDisconnect() = synchronized(lock) {
        if (useCount.decrementAndGet() == 0) {
            connection?.close()
            connection = null
        }
    }

    fun <T> useConnection(block: StatementCreator.(connection: Connection, savepoint: Savepoint) -> T?): T? {
        val conn = getOrConnect()
        val statementCreator = StatementCreator(conn)
        val savepoint = conn.setSavepoint()
        try {
            return statementCreator.block(conn, savepoint)
        } finally {
            conn.releaseSavepoint(savepoint)
            releaseOrDisconnect()
        }
    }

    override fun close() = synchronized(lock) {
        connection?.close()
        connection = null
        useCount.set(0)
    }
}