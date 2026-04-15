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

class MiniMessageSanitizerTest {
    @Test
    fun testSanitizeEventsOnlyRemovesEventTagsCaseInsensitiveAndQuotedArguments() {
        val input = "<CLICK:run_command:/seed>Hello<hover:show_text:\"a > b\">!</HOVER></CLICK>"

        assertEquals("Hello!", MiniMessageSanitizer.sanitizeEventsOnly(input))
    }

    @Test
    fun testSanitizeEventsOnlyKeepsNonEventTags() {
        val input = "<red>Hello</red> <#00ff00>world</#00ff00>"

        assertEquals(input, MiniMessageSanitizer.sanitizeEventsOnly(input))
    }

    @Test
    fun testSanitizeEventsOnlyUnescapesEscapedTagOpenersInPlainText() {
        val input = "Use \\<red>literal</red> and <insert:test>this</insert>"

        assertEquals("Use <red>literal</red> and this", MiniMessageSanitizer.sanitizeEventsOnly(input))
    }

    @Test
    fun testSanitizeRemovesAllMiniMessageTagsIncludingQuotedArguments() {
        val input = "<yellow>Hello <hover:show_text:\"a > b\">World</hover>!</yellow>"

        assertEquals("Hello World!", MiniMessageSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeKeepsInvalidOrIncompleteTagsAsText() {
        val input = "Math: 1 < 2 and <#12> and <red"

        assertEquals(input, MiniMessageSanitizer.sanitize(input))
    }

    @Test
    fun testSanitizeRemovesSelfClosingTags() {
        val input = "You have <score:rymiel:gamesWon/> games"

        assertEquals("You have  games", MiniMessageSanitizer.sanitize(input))
    }
}

