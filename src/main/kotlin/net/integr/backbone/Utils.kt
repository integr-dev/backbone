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

import net.kyori.adventure.builder.AbstractBuilder
import kotlin.reflect.full.declaredMemberFunctions

/**
 * Utility functions for various tasks.
 * @since 1.0.0
 */
object Utils {
    /**
     * Executes the given block and returns its result, or null if an exception occurs.
     *
     * @param block The block to execute.
     * @return The result of the block, or null if an exception occurred.
     * @since 1.0.0
     */
    fun <T> tryOrNull(block: () -> T): T? {
        return try {
            block()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if the given string is valid snake_case.
     *
     * @param string The string to check.
     * @return True if the string is valid snake_case, false otherwise.
     * @since 1.0.0
     */
    fun isSnakeCase(string: String): Boolean {
        return string.matches("^[a-z0-9]+(_[a-z0-9]+)*$".toRegex())
    }

    /**
     * Checks if the given string is a valid UID.
     *
     * @param string The string to check.
     * @return True if the string is a valid UID, false otherwise.
     * @since 1.0.0
     */
    fun isUid(string: String): Boolean {
        return string.matches("^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$".toRegex())
    }

    /**
     * Used to more easily get the result of a builder with applied block.
     *
     * Example:
     * ```kotlin
     * val builder = Something.builder()
     * builder.block()
     * val result = builder.build()
     * ```
     *
     * is changed to
     *
     * ```kotlin
     *  val result = blockBuild(Something.builder(), block)
     *  ```
     *
     * Invokes a builders build method via reflection.
     * Does not run any safety checks. It is your job to figure out
     * if this will work or not.
     *
     * @param T the builder class
     * @param U the builders result class
     * @param builder the builder instance
     * @param block the block to apply to the builder
     * @since 1.4.0
     */
    inline fun <reified T : Any, U> blockBuild(builder: T, block: T.() -> Unit): U {
        builder.block()
        // Assume a build method is there
        val method = builder::class.java.getDeclaredMethod("build")

        // It is the users duty to only call this on builders with this signature
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(builder) as U
        return result
    }
}