package net.integr.backbone.systems.storage.database

import net.integr.backbone.Backbone
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

class StatementCreator(private val connection: Connection) {
    private val logger = Backbone.LOGGER.derive("statement-creator")

    fun <T> useStatement(block: (Statement) -> T): T? {
        try {
            val statement = connection.createStatement()
            statement.use {
                return block(statement)
            }
        } catch (e: SQLException) {
            logger.severe("Exception in SQL statement: ${e.message}")
            return null
        }
    }

    fun <T> usePreparedStatement(@Language("SQL") sql: String, block: (PreparedStatement) -> T): T? {
        try {
            @Suppress("SqlSourceToSinkFlow") // String is just passed along
            val statement = connection.prepareStatement(sql)
            statement.use {
                return block(statement)
            }
        } catch (e: SQLException) {
            logger.severe("Exception in SQL statement: ${e.message}")
            return null
        }
    }
}