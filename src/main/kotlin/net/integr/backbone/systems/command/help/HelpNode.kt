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

package net.integr.backbone.systems.command.help

import net.integr.backbone.systems.text.component
import net.kyori.adventure.text.Component
import java.awt.Color

/**
 * Represents a hierarchical node in the command help system.
 * 
 * This class builds a tree structure that displays command information in a formatted, visually organized manner.
 * Each node can contain content (descriptions, arguments, aliases) and child nodes (subcommands).
 * The tree structure is rendered with proper indentation, tree branches, and color coding for easy readability.
 *
 * @param title The title of this help node (e.g., command name).
 * @param contents A list of content items to display under the title.
 * @param children A list of child nodes representing subcommands.
 * @since 1.3.0
 */
class HelpNode(val title: String, val contents: List<Content>, val children: List<HelpNode>) {

    /**
     * Builds a formatted text component representation of this help node and its entire tree.
     * 
     * This method recursively renders the help node as an Adventure text component with proper tree structure,
     * indentation, color coding, and formatting. The tree is displayed with branch characters (├, └) and
     * vertical lines (│) to show the hierarchy. Content sections are separated with visual separators (─).
     *
     * @param prefix The prefix string for indentation of nested nodes. Defaults to empty string for the root node.
     * @param isLast Whether this node is the last child of its parent. Affects branch character selection.
     * @param isRoot Whether this is the root node of the tree. Determines initial branch style.
     * @return A formatted Component representing this help node and all its children.
     * @since 1.3.0
     */
    fun buildComponent(prefix: String = "", isLast: Boolean = true, isRoot: Boolean = true): Component {
        val titleColor = Color(141, 184, 130)
        val barColor = Color(100, 120, 90)  // Dark green for tree bars
        
        // Tree structure branches
        val branch = if (isRoot) "» " else if (isLast) "└ » " else "├ » "
        
        // Continuation for nested children
        val childPrefix = if (isRoot) "" else prefix + if (isLast) "  " else "│ "
        
        // Content prefix - vertical bar with separator/content
        val contentPrefix = if (isRoot) "│ " else "$childPrefix│ "
        val verticalBar = if (isRoot) "│" else "$childPrefix│"

        return component {
            // Title
            append(prefix) { color(barColor) }
            append(branch) { color(barColor) }
            append("$title\n") { color(titleColor) }
            
            // Separator line
            append(contentPrefix + "─".repeat(10) + "\n") { color(barColor) }

            // Contents
            val hasChildren = children.isNotEmpty()
            val lastContentIndex = contents.size - 1
            
            contents.forEachIndexed { idx, content ->
                if (content.type == Content.Type.TITLE) {
                    append("$verticalBar\n") { color(barColor) }
                }
                
                val isLastContent = idx == lastContentIndex && !hasChildren
                val contentBranch = if (isLastContent) "└ " else "│ "
                val contentLine = if (isRoot) contentBranch else childPrefix + contentBranch
                
                append(contentLine) { color(barColor) }
                append("$content\n") { color(content.getColor()) }
            }

            // Children with tree structure - parent manages ALL spacing
            if (children.isNotEmpty()) {
                // Blank line after content, before first child
                append("$verticalBar\n") { color(barColor) }
                
                children.forEachIndexed { index, child ->
                    val childIsLast = index == children.size - 1
                    append(child.buildComponent(childPrefix, childIsLast, isRoot = false))
                    
                    // Add a blank line after each child (except the last)
                    if (!childIsLast) {
                        append("$verticalBar\n") { color(barColor) }
                    }
                }
            }
        }
    }

    /**
     * Represents a single content item within a help node.
     * 
     * Content items can be text descriptions, section titles, or list items. Each content item
     * is rendered with appropriate formatting and color based on its type.
     *
     * @param text The text content to display.
     * @param type The type of content, determining how it's formatted and colored.
     * @since 1.3.0
     */
    class Content(val text: String, val type: Type) {
        /**
         * Returns a string representation of this content item.
         * 
         * Formatting depends on the content type:
         * - TEXT and TITLE are returned as-is
         * - LIST items are prefixed with a bullet point (•)
         *
         * @return The formatted string representation of this content.
         * @since 1.3.0
         */
        override fun toString(): String {
            return when (type) {
                Type.TEXT, Type.TITLE -> text
                Type.LIST -> " • $text"
            }
        }

        /**
         * Gets the display color for this content item based on its type.
         * 
         * @return A Color object representing the appropriate color for this content type.
         * @since 1.3.0
         */
        fun getColor(): Color {
            return when (type) {
                Type.TEXT, Type.LIST -> Color(169, 173, 168)
                Type.TITLE -> Color(141, 184, 130)
            }
        }

        /**
         * Enumeration of possible content types.
         * 
         * @since 1.3.0
         */
        enum class Type {
            /** Regular text content such as descriptions. */
            TEXT,
            /** List item content, typically used for arguments or aliases. */
            LIST,
            /** Section title content that groups related items. */
            TITLE
        }
    }
}