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

/**
 * Represents a permission node within the Backbone permission system.
 *
 * Permission nodes are hierarchical strings (e.g., "backbone.command.help") that define specific
 * access rights. This class provides a way to create and manage these nodes, including deriving
 * child nodes and comparing them.
 *
 * @param id The string representation of the permission node (e.g., "backbone.command.help").
 * @since 1.0.0
 */
class PermissionNode(val id: String) {
    /**
     * Derives a new [PermissionNode] by appending a child ID to the current node's ID.
     *
     * For example, if the current node's ID is "backbone.command" and the child ID is "help",
     * the derived node's ID will be "backbone.command.help".
     *
     * @param id The ID of the child node to append.
     * @return A new [PermissionNode] representing the derived child node.
     * @since 1.0.0
     */
    fun derive(id: String): PermissionNode {
        return PermissionNode("${this.id}.$id")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionNode

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}