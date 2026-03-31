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

package net.integr.backbone.systems.placeholder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlaceholderHelperTest {
    // Assume the id we have is "hello" in group "mygroup"
    // Then the full placeholder would be %mygroup_hello%
    // if we receive %mygroup_hello%, then we should match
    // if we receive %mygroup_hello_world%, then we should also match, and pass "world" as an argument to the placeholder
    // if we receive %mygroup_hello_world_again%, then we should also match, and pass "world_again" as an argument to the placeholder
    // if we receive %mygroup_helloa%, then we should not match, because there is no underscore after "hello", if we had a placeholder with id "helloa", then we would match, and pass an empty string as an argument to the placeholder

    // Assume the id we have is "hello_world" in group "mygroup"
    // Then the full placeholder would be %mygroup_hello_world%
    // if we receive %mygroup_hello_world%, then we should match
    // if we receive %mygroup_hello_world_again%, then we should also match, and pass "again" as an argument to the placeholder
    // if we receive %mygroup_hello_worlda%, then we should not match, because there is no underscore after "hello_world", if we had a placeholder with id "hello_worlda", then we would match, and pass an empty string as an argument to the placeholder


    fun testMatch(params: String, targetPlaceHolder: String, expectedResult: Boolean) {
        val map = setOf(targetPlaceHolder)

        val result = PlaceholderHelper.match(map, params)
        assertEquals(expectedResult, result != null)
    }

    @Test
    fun testMatchSimple() {
        testMatch("hello", "hello", true)
        testMatch("hello_world", "hello", true)
        testMatch("hello_world_again", "hello", true)
        testMatch("helloa", "hello", false)
    }

    @Test
    fun testMatchComplex() {
        testMatch("hello_world", "hello_world", true)
        testMatch("hello_world_again", "hello_world", true)
        testMatch("hello_worlda", "hello_world", false)
    }

    @Test
    fun testMatchMultiple() {
        val map = setOf("hello", "hello_world")

        val result1 = PlaceholderHelper.match(map, "hello_world")
        assertEquals("hello_world", result1)

        val result2 = PlaceholderHelper.match(map, "hello_world_again")
        assertEquals("hello_world", result2)

        val result3 = PlaceholderHelper.match(map, "helloa")
        assertNull(result3)
    }
}