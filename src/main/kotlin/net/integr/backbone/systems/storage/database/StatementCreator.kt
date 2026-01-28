package net.integr.backbone.systems.storage.database

import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class StatementCreator(private val connection: Connection) {
    fun <T> useStatement(block: (Statement) -> T): T{
        val statement = connection.createStatement()
        statement.use {
            return block(statement)
        }
    }

    fun <T> usePreparedStatement(@Language("SQL") sql: String, block: (PreparedStatement) -> T): T {
        @Suppress("SqlSourceToSinkFlow") // String is just passed along
        val statement = connection.prepareStatement(sql)
        statement.use {
            return block(statement)
        }
    }
}