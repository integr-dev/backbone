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

package net.integr.backbone.systems.permission

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PermissionNodeTest {
    @Test
    fun testDerive() {
        val node = PermissionNode("root")
        val derivedNode = node.derive("child")

        assertEquals("root.child", derivedNode.id)
    }

    @Test
    fun testEquals() {
        val node1 = PermissionNode("root")
        val node2 = PermissionNode("root")
        val node3 = PermissionNode("other")

        assertEquals(node1, node2)
        assertNotEquals(node1, node3)
    }
}