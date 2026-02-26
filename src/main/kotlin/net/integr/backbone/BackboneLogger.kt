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

package net.integr.backbone

import org.bukkit.plugin.java.JavaPlugin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.*

class BackboneLogger(name: String, private val plugin: JavaPlugin?) : Logger(name, null) {
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    init {
        useParentHandlers = false
        level = Level.ALL

        addHandler(CustomHandler())
    }

    fun derive(subName: String): BackboneLogger {
        return BackboneLogger("$name.$subName", plugin)
    }

    private inner class CustomHandler : Handler() {
        override fun publish(record: LogRecord) {
            if (!isLoggable(record) || plugin == null) return
            val message = format(record)
            println(message)
        }

        override fun flush() {}
        override fun close() {}
    }

    fun format(record: LogRecord): String {
        val builder = StringBuilder()

        builder.append(dateFormat.format(Date(record.millis)))
        builder.append(" ")

        when (record.level) {
            Level.SEVERE -> {
                builder.append(ANSI_BACKGROUND_RED)
                builder.append(ANSI_BLACK)
                builder.append("ERRO")
                builder.append(ANSI_RESET)
                builder.append(ANSI_RED)
            }
            Level.WARNING -> {
                builder.append(ANSI_BACKGROUND_YELLOW)
                builder.append(ANSI_BLACK)
                builder.append("WARN")
                builder.append(ANSI_RESET)
                builder.append(ANSI_YELLOW)
            }
            Level.INFO -> {
                builder.append(ANSI_BACKGROUND_CYAN)
                builder.append(ANSI_BLACK)
                builder.append("INFO")
                builder.append(ANSI_RESET)
                builder.append(ANSI_CYAN)
            }
        }

        builder.append(" [")
        builder.append(name)
        builder.append("]")

        builder.append(ANSI_BLACK)
        builder.append(" - ")
        builder.append(ANSI_WHITE)
        builder.append(record.message)

        val params = record.parameters

        if (params != null) {
            builder.append("\t")
            for (i in params.indices) {
                builder.append(params[i])
                if (i < params.size - 1) builder.append(", ")
            }
        }

        builder.append(ANSI_RESET)
        return builder.toString()
    }


    companion object {
        const val ANSI_RESET: String = "\u001B[0m"
        const val ANSI_BLACK: String = "\u001B[30m"
        const val ANSI_RED: String = "\u001B[31m"
        const val ANSI_GREEN: String = "\u001B[32m"
        const val ANSI_YELLOW: String = "\u001B[33m"
        const val ANSI_BLUE: String = "\u001B[34m"
        const val ANSI_PURPLE: String = "\u001B[35m"
        const val ANSI_CYAN: String = "\u001B[36m"
        const val ANSI_WHITE: String = "\u001B[37m"

        const val ANSI_BACKGROUND_BLACK: String = "\u001B[40m"
        const val ANSI_BACKGROUND_RED: String = "\u001B[41m"
        const val ANSI_BACKGROUND_GREEN: String = "\u001B[42m"
        const val ANSI_BACKGROUND_YELLOW: String = "\u001B[43m"
        const val ANSI_BACKGROUND_BLUE: String = "\u001B[44m"
        const val ANSI_BACKGROUND_PURPLE: String = "\u001B[45m"
        const val ANSI_BACKGROUND_CYAN: String = "\u001B[46m"
        const val ANSI_BACKGROUND_WHITE: String = "\u001B[47m"
    }
}