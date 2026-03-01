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

import net.integr.backbone.Backbone
import net.integr.backbone.systems.storage.ResourceLocation
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Savepoint
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages a connection to a SQLite database, providing connection pooling and transaction management.
 *
 * @param db The `ResourceLocation` representing the database file.
 *
 * @since 1.0.0
 */
class DatabaseConnection(db: ResourceLocation) : AutoCloseable {
    private val logger = Backbone.LOGGER.derive("database-connection")

    private val jdbcUrl = "jdbc:sqlite:" + db.location.absolutePath
    private var connection: Connection? = null

    /**
     * The number of active uses of this connection.
     *
     * @since 1.0.0
     */
    private val useCount = AtomicInteger(0)
    private val lock = Any()

    /**
     * Retrieves an existing connection or establishes a new one if necessary.
     *
     * @return The active `Connection` object.
     *
     * @since 1.0.0
     */
    fun getOrConnect(): Connection = synchronized(lock) {
        if (connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(jdbcUrl)
            logger.info("Connection to '$jdbcUrl' opened.")
        }

        useCount.incrementAndGet()
        return connection!!
    }

    /**
     * Releases the connection if it's no longer in use, or decrements the usage count.
     *
     * @since 1.0.0
     */
    fun releaseOrDisconnect() = synchronized(lock) {
        if (useCount.decrementAndGet() == 0) {
            connection?.close()
            connection = null
            logger.info("Connection to '$jdbcUrl' closed.")
        }
    }

    /**
     * Executes a block of code with a database connection, handling connection pooling and transactions.
     *
     * @param block The block of code to execute, receiving a `StatementCreator`, `Connection`, and `Savepoint`.
     * @return The result of the `block` execution.
     *
     * @since 1.0.0
     */
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

    /**
     * Forcefully closes the connection, regardless of the usage count.
     *
     * @since 1.0.0
     */
    override fun close() = synchronized(lock) {
        connection?.close()
        connection = null
        useCount.set(0)
    }
}