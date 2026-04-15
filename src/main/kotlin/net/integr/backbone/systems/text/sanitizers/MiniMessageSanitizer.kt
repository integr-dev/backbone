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

package net.integr.backbone.systems.text.sanitizers

/**
 * Sanitizes MiniMessage-formatted text before it is shown or persisted.
 *
 * This sanitizer supports two modes:
 * - [sanitizeEventsOnly], which removes only interactive tags such as click and hover.
 * - [sanitize], which removes all recognized MiniMessage tags.
 *
 * Parsing is performed with a lightweight scanner so quoted tag arguments are handled safely
 * (for example, `>` inside quoted hover text), and escaped tag openers (`\<`) are preserved
 * as literal text.
 *
 * @since 1.9.0
 */
object MiniMessageSanitizer {
    private val EVENT_TAGS = setOf("click", "hover", "insert")

    /**
     * Remove only MiniMessage event tags from input while keeping non-event formatting tags.
     *
     * This is intended for contexts where colors/decorations are acceptable, but interactive
     * behavior must be stripped.
     *
     * @param input Raw text that may contain MiniMessage tags.
     * @return A sanitized string without event tags.
     * @since 1.9.0
     */
    fun sanitizeEventsOnly(input: String): String {
        return sanitizeInternal(input, removeAllTags = false)
    }

    /**
     * Remove all recognized MiniMessage tags from input.
     *
     * @param input Raw text that may contain MiniMessage tags.
     * @return Plain text with all recognized tags removed.
     * @since 1.9.0
     */
    fun sanitize(input: String): String {
        return sanitizeInternal(input, removeAllTags = true)
    }

    /**
     * Shared sanitization routine used by public sanitize modes.
     *
     * @param input Raw input text.
     * @param removeAllTags When true, removes every recognized tag; otherwise only event tags.
     * @return Sanitized text according to the selected mode.
     * @since 1.9.0
     */
    private fun sanitizeInternal(input: String, removeAllTags: Boolean): String {
        val output = StringBuilder(input.length)
        var index = 0

        while (index < input.length) {
            val current = input[index]

            if (current == '\\' && index + 1 < input.length && input[index + 1] == '<') {
                output.append('<')
                index += 2
                continue
            }

            if (current != '<') {
                output.append(current)
                index++
                continue
            }

            val parsedTag = parseTag(input, index)
            if (parsedTag == null) {
                output.append(current)
                index++
                continue
            }

            val removeTag = removeAllTags || parsedTag.name in EVENT_TAGS
            if (!removeTag) {
                output.append(parsedTag.raw)
            }

            index = parsedTag.endExclusive
        }

        return output.toString()
    }

    /**
     * Parse a MiniMessage-like tag starting at a `<` character.
     *
     * Tag termination honors quoted segments so `>` only closes the tag when outside
     * single-quoted or double-quoted values.
     *
     * @param input Full input being scanned.
     * @param startIndex Index of the opening `<`.
     * @return Parsed tag metadata, or null if the sequence is not a valid supported tag.
     * @since 1.9.0
     */
    private fun parseTag(input: String, startIndex: Int): ParsedTag? {
        var index = startIndex + 1
        var quote: Char? = null

        while (index < input.length) {
            val current = input[index]

            if (quote != null) {
                if (current == '\\' && index + 1 < input.length && (input[index + 1] == quote || input[index + 1] == '\\')) {
                    index += 2
                    continue
                }

                if (current == quote) {
                    quote = null
                }

                index++
                continue
            }

            when (current) {
                '\'', '"' -> quote = current
                '>' -> {
                    val content = input.substring(startIndex + 1, index)
                    val name = extractTagName(content) ?: return null
                    return ParsedTag(
                        name = name.lowercase(),
                        raw = input.substring(startIndex, index + 1),
                        endExclusive = index + 1,
                    )
                }
            }

            index++
        }

        return null
    }

    /**
     * Extract and normalize the tag name token from raw tag content.
     *
     * Supports opening, closing, inverted (`!tag`) and self-closing (`tag/`) forms,
     * and validates standard name/hex-color forms used by MiniMessage.
     *
     * @param content Raw content inside `<` and `>`.
     * @return The parsed tag name, or null when the content should be treated as plain text.
     * @since 1.9.0
     */
    private fun extractTagName(content: String): String? {
        var token = content.trim()
        if (token.isEmpty()) return null

        if (token.startsWith('/')) {
            token = token.substring(1).trimStart()
        }

        if (token.startsWith('!')) {
            token = token.substring(1)
        }

        if (token.isEmpty()) return null

        val delimiterIndex = token.indexOf(':')
        var name = if (delimiterIndex == -1) token else token.substring(0, delimiterIndex)

        if (name.endsWith('/')) {
            name = name.dropLast(1)
        }

        if (name.isEmpty()) return null

        if (name.first() == '#') {
            val hex = name.drop(1)
            if (hex.length != 6 || !hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                return null
            }
            return name
        }

        if (!name.first().isLetter()) return null
        if (!name.all { it.isLetterOrDigit() || it == '_' }) return null

        return name
    }

    /**
     * Internal representation of a parsed MiniMessage tag.
     *
     * @property name Lowercased tag name used for matching.
     * @property raw Original source text for the parsed tag.
     * @property endExclusive End index (exclusive) in the source string.
     * @since 1.9.0
     */
    private data class ParsedTag(
        val name: String,
        val raw: String,
        val endExclusive: Int,
    )
}