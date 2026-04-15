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

package net.integr.backbone.systems.persistence.database

import net.integr.backbone.Backbone
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/**
 * A utility class for creating and executing SQL statements and prepared statements.
 *
 * @param connection The `Connection` object to use for creating statements.
 *
 * @since 1.0.0
 */
class StatementCreator(private val connection: Connection) {
    private val logger = Backbone.LOGGER.derive("statement-creator")

    /**
     * Creates and executes a `Statement`, then processes its result.
     *
     * @param block The block of code to execute with the `Statement` object.
     * @return The result of the `block` execution, or `null` if an SQL exception occurs.
     *
     * @since 1.0.0
     */
    fun <T> statement(block: Statement.() -> T): T? {
        try {
            val statement = connection.createStatement()
            statement.use {
                return statement.block()
            }
        } catch (e: SQLException) {
            logger.severe("Exception in SQL statement: ${e.message}")
            return null
        }
    }

    /**
     * Executes a raw SQL query.
     *
     * @param sql The SQL query string.
     * @return `true` if the first result is a `ResultSet` object; `false` if the first result is an update count or there is no result.
     * @since 1.0.0
     */
    fun execute(@Language("SQL") sql: String): Boolean? {
        logger.info("Executing SQL: $sql")
        return statement {
            @Suppress("SqlSourceToSinkFlow") // String is just passed along
            this.execute(sql)
        }
    }

    /**
     * Runs a query and maps the results to an object.
     *
     * @param sql The SQL query string.
     * @param mapper A function to map the `ResultSet` to an object of type `T`.
     * @return The mapped object, or `null` if no result is found or an SQL exception occurs.
     *
     * @since 1.0.0
     */
    fun <T> query(@Language("SQL") sql: String, mapper: (ResultSet) -> T?): T? {
        logger.info("Executing SQL: $sql")
        return statement {
            @Suppress("SqlSourceToSinkFlow") // String is just passed along
            val res = this.executeQuery(sql)
            res.use {
                if (res.next()) {
                    return@statement mapper(res)
                } else {
                    return@statement null
                }
            }
        }
    }

    /**
     * Create a prepared statement.
     *
     * @param sql The SQL query string.
     * @param block The block of code to execute with the `PreparedStatement` object.
     * @return The result of the `block` execution, or `null` if an SQL exception occurs.
     *
     * @since 1.0.0
     */
    fun <T> preparedStatement(@Language("SQL") sql: String, block: PreparedStatement.() -> T): T? {
        logger.info("Executing SQL: $sql")
        try {
            @Suppress("SqlSourceToSinkFlow") // String is just passed along
            val statement = connection.prepareStatement(sql)
            statement.use {
                return statement.block()
            }
        } catch (e: SQLException) {
            logger.severe("Exception in SQL statement: ${e.message}")
            return null
        }
    }
}