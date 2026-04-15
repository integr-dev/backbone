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
 * Sanitizes PlaceholderAPI-style placeholder tokens from text.
 *
 * This sanitizer removes any closed `%...%` token and leaves incomplete tokens untouched,
 * which allows partially typed placeholders to pass through as plain text.
 *
 * @since 1.9.0
 */
object PlaceholderSanitizer {
    /**
     * Remove closed placeholder tokens from the provided input.
     *
     * Placeholders are matched using the `%[^%]+%` pattern, which strips any non-empty token
     * wrapped by `%` characters.
     *
     * @param input Raw text that may contain placeholder tokens.
     * @return The input string with closed placeholders removed.
     * @since 1.9.0
     */
    fun sanitize(input: String): String {
        // Hello %placeholder% world -> Hello  world
        // Hello %placeholder% world %another% -> Hello  world
        // Hello %placeholder% world %another -> Hello  world %another
        // Hello %placeholder world -> Hello %placeholder world
        // Hello %placeholder%world -> Hello world
        // Hello %placeholder% world%another% -> Hello  world

        return input.replace(Regex("%[^%]+%"), "")
    }
}