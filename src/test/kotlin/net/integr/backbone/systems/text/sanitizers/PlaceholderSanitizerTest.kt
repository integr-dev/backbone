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

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderSanitizerTest {
    @Test
    fun testSanitizeRemovesSinglePlaceholder() {
        val input = "Hello %placeholder% world"

        assertEquals("Hello  world", PlaceholderSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeRemovesMultipleClosedPlaceholders() {
        val input = "Hello %placeholder% world %another%"

        assertEquals("Hello  world ", PlaceholderSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeRemovesOnlyClosedPlaceholderWhenMixed() {
        val input = "Hello %placeholder% world %another"

        assertEquals("Hello  world %another", PlaceholderSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeKeepsUnclosedPlaceholder() {
        val input = "Hello %placeholder world"

        assertEquals(input, PlaceholderSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeHandlesAdjacentPlaceholderAndWord() {
        val input = "Hello %placeholder%world"

        assertEquals("Hello world", PlaceholderSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeHandlesPlaceholderAtWordBoundary() {
        val input = "Hello %placeholder% world%another%"

        assertEquals("Hello  world", PlaceholderSanitizer.sanitize(input))
    }
}

