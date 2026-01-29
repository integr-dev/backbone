package net.integr.backbone.systems.storage.database

import net.integr.backbone.Backbone
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class StatementCreator(private val connection: Connection) {
    private val logger = Backbone.LOGGER.derive("statement-creator")

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

    fun execute(@Language("SQL") sql: String): Boolean? {
        return statement {
            @Suppress("SqlSourceToSinkFlow") // String is just passed along
            this.execute(sql)
        }
    }

    fun <T> query(@Language("SQL") sql: String, mapper: (ResultSet) -> T?): T? {
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

    fun <T> preparedStatement(@Language("SQL") sql: String, block: PreparedStatement.() -> T): T? {
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