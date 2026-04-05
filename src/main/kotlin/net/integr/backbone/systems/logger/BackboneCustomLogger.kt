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

package net.integr.backbone.systems.logger

import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.*
import kotlin.io.path.appendText
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

/**

 * A custom logger for the Backbone plugin.
 *
 * This logger provides colored console output and logs severe messages to a file.
 * It also allows for deriving sub-loggers with specific names.
 *
 * @param name The name of the logger.
 *
 * @since 1.0.0
 */
class BackboneCustomLogger(name: String) : Logger(name, null) {
    private val logFile = Path.of("./logs/backbone.log")
    private val customFormat = CustomFormat(name, true)
    private val customFileFormat = CustomFormat(name, false)

    init {
        logFile.parent.createDirectories()
        if (!logFile.toFile().exists()) {
            logFile.createFile()
        }

        useParentHandlers = false
        level = Level.ALL

        addHandler(CustomHandler())
    }


    private inner class CustomHandler : Handler() {
        override fun publish(record: LogRecord) {
            if (!isLoggable(record)) return
            val message = customFormat.format(record)
            val fileMessage = customFileFormat.format(record)
            println(message)
            if (record.level == Level.SEVERE) logFile.appendText(fileMessage + "\n")
        }

        /**
         * Flushes any buffered output.
         *
         * This implementation is intentionally empty as the handler writes directly to
         * the console (via `println`) and file (via `appendText`), both of which handle
         * their own flushing automatically.
         *
         * @since 1.0.0
         */
        override fun flush() {}

        /**
         * Closes the handler and releases any associated resources.
         *
         * This implementation is intentionally empty as the handler does not maintain
         * any resources that require explicit cleanup. The log file is opened and closed
         * on each write operation, and console output requires no cleanup.
         *
         * @since 1.0.0
         */
        override fun close() {}
    }

    class CustomFormat(val name: String, val color: Boolean) : Formatter() {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")

        override fun format(record: LogRecord): String {
            val builder = StringBuilder()

            builder.append(dateFormat.format(Date(record.millis)))
            builder.append(" ")

            when (record.level) {
                Level.SEVERE -> {
                    if (color) builder.append(ANSI_BACKGROUND_RED)
                    if (color) builder.append(ANSI_BLACK)
                    builder.append("ERRO")
                    if (color) builder.append(ANSI_RESET)
                    if (color) builder.append(ANSI_RED)
                }
                Level.WARNING -> {
                    if (color) builder.append(ANSI_BACKGROUND_YELLOW)
                    if (color) builder.append(ANSI_BLACK)
                    builder.append("WARN")
                    if (color) builder.append(ANSI_RESET)
                    if (color) builder.append(ANSI_YELLOW)
                }
                Level.INFO -> {
                    if (color) builder.append(ANSI_BACKGROUND_CYAN)
                    if (color) builder.append(ANSI_BLACK)
                    builder.append("INFO")
                    if (color) builder.append(ANSI_RESET)
                    if (color) builder.append(ANSI_CYAN)
                }
            }

            builder.append(" [")
            builder.append(name)
            builder.append("]")

            if (color) builder.append(ANSI_BLACK)
            builder.append(" - ")
            if (color) builder.append(ANSI_WHITE)
            builder.append(record.message)

            val params = record.parameters

            if (params != null) {
                builder.append("\t")
                for (i in params.indices) {
                    builder.append(params[i])
                    if (i < params.size - 1) builder.append(", ")
                }
            }

            if (color) builder.append(ANSI_RESET)
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
}