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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun testIsSnakeCase() {
        assertTrue(Utils.isSnakeCase("test_string"))
        assertTrue(Utils.isSnakeCase("another_test_string_123"))
        assertTrue(Utils.isSnakeCase("singleword"))
        assertFalse(Utils.isSnakeCase("TestString"))
        assertFalse(Utils.isSnakeCase("test-string"))
        assertFalse(Utils.isSnakeCase("test String"))
        assertFalse(Utils.isSnakeCase("_test_string"))
        assertFalse(Utils.isSnakeCase("test_string_"))
        assertFalse(Utils.isSnakeCase(""))
    }

    @Test
    fun testIsUid() {
        assertTrue(Utils.isUid("a1b2c3d4-e5f6-7890-1234-567890abcdef"))
        assertTrue(Utils.isUid("00000000-0000-0000-0000-000000000000"))
        assertFalse(Utils.isUid("a1b2c3d4-e5f6-7890-1234-567890abcde")) // Too short
        assertFalse(Utils.isUid("a1b2c3d4-e5f6-7890-1234-567890abcdefg")) // Too long
        assertFalse(Utils.isUid("a1b2c3d4_e5f6-7890-1234-567890abcdef")) // Wrong separator
        assertFalse(Utils.isUid("a1b2c3d4-e5f6-7890-1234-567890abcdeF")) // Uppercase hex
        assertFalse(Utils.isUid("not-a-uuid"))
        assertFalse(Utils.isUid(""))
    }

    private class TestBuilder {
        private var value: String = ""

        fun setValue(v: String) {
            value = v
        }

        fun build(): String {
            return "Built: $value"
        }
    }

    @Test
    fun testBlockBuild() {
        val result: String = Utils.blockBuild(TestBuilder()) {
            setValue("Hello, World!")
        }
        assertEquals("Built: Hello, World!", result)
    }
}