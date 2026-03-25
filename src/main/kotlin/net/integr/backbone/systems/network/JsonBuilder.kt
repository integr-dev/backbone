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

package net.integr.backbone.systems.network

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode

/**
 * A builder utility for constructing JSON trees using a DSL-like syntax.
 * Provides methods for building objects, arrays, and adding values.
 *
 * @since 1.7.0
 */
class JsonBuilder {
    private val nodeStack = mutableListOf<JsonNode>()
    private val root: JsonNode = JsonNodeFactory.instance.objectNode()

    init {
        nodeStack.add(root)
    }

    /**
     * Adds a named object as a field in the current object or array.
     * @param name The field name.
     * @param block The block to build the object.
     * @since 1.7.0
     */
    fun obj(name: String, block: JsonBuilder.() -> Unit) {
        val obj = JsonNodeFactory.instance.objectNode()
        nodeStack.add(obj)
        this.block()
        nodeStack.removeLast()
        addToParent(name, obj)
    }

    /**
     * Adds an anonymous object (for use inside arrays).
     * @param block The block to build the object.
     * @since 1.7.0
     */
    fun obj(block: JsonBuilder.() -> Unit) {
        val obj = JsonNodeFactory.instance.objectNode()
        nodeStack.add(obj)
        this.block()
        nodeStack.removeLast()
        addToParent(obj)
    }

    /**
     * Adds a named array as a field in the current object or array.
     * @param name The field name.
     * @param block The block to build the array.
     * @since 1.7.0
     */
    fun arr(name: String, block: JsonBuilder.() -> Unit) {
        val arr = JsonNodeFactory.instance.arrayNode()
        nodeStack.add(arr)
        this.block()
        nodeStack.removeLast()
        addToParent(name, arr)
    }

    /**
     * Adds an anonymous array (for use inside arrays).
     * @param block The block to build the array.
     * @since 1.7.0
     */
    fun arr(block: JsonBuilder.() -> Unit) {
        val arr = JsonNodeFactory.instance.arrayNode()
        nodeStack.add(arr)
        this.block()
        nodeStack.removeLast()
        addToParent(arr)
    }

    /**
     * Puts a field in the current object.
     * @param name The field name.
     * @param value The value to set.
     * @since 1.7.0
     */
    fun put(name: String, value: Any?) {
        val v = toJsonNode(value)
        val current = nodeStack.last()
        if (current is ObjectNode) {
            current.set(name, v)
        } else {
            throw IllegalStateException("put(name, value) can only be used inside an object")
        }
    }

    /**
     * Puts a value in the current array.
     * @param value The value to add.
     * @since 1.7.0
     */
    fun value(value: Any?) {
        val v = toJsonNode(value)
        val current = nodeStack.last()
        if (current is ArrayNode) {
            current.add(v)
        } else {
            throw IllegalStateException("put(value) can only be used inside an array")
        }
    }

    private fun addToParent(name: String, child: JsonNode) {
        when (val parent = nodeStack.last()) {
            is ObjectNode -> parent.set(name, child)
            is ArrayNode -> parent.add(child)
            else -> throw IllegalStateException("Parent must be object or array")
        }
    }

    private fun addToParent(child: JsonNode) {
        when (val parent = nodeStack.last()) {
            is ArrayNode -> parent.add(child)
            else -> throw IllegalStateException("Anonymous obj/arr can only be added to arrays")
        }
    }

    private fun toJsonNode(v: Any?): JsonNode = when (v) {
        null -> JsonNodeFactory.instance.nullNode()
        is String -> JsonNodeFactory.instance.stringNode(v)
        is Int -> JsonNodeFactory.instance.numberNode(v)
        is Long -> JsonNodeFactory.instance.numberNode(v)
        is Double -> JsonNodeFactory.instance.numberNode(v)
        is Float -> JsonNodeFactory.instance.numberNode(v)
        is Boolean -> JsonNodeFactory.instance.booleanNode(v)
        is ObjectNode -> v
        else -> JsonNodeFactory.instance.stringNode(v.toString())
    }

    /**
     * Builds and returns the root JsonNode.
     * @return The constructed JsonNode.
     * @since 1.7.0
     */
    fun build(): JsonNode = root

    companion object {
        /**
         * The shared JsonMapper instance for JSON serialization/deserialization.
         * @since 1.7.0
         */
        val JSON: JsonMapper = JsonMapper
            .builder()
            .findAndAddModules()
            .build()
    }
}

/**
 * Builds a JsonNode using the JsonBuilder DSL.
 * @param block The builder block.
 * @return The constructed JsonNode.
 * @since 1.7.0
 */
fun jsonTree(block: JsonBuilder.() -> Unit): JsonNode {
    val builder = JsonBuilder()
    builder.block()
    return builder.build()
}

/**
 * Builds a JSON string using the JsonBuilder DSL.
 * @param block The builder block.
 * @return The constructed JSON string.
 * @since 1.7.0
 */
fun json(block: JsonBuilder.() -> Unit): String {
    return JsonBuilder.JSON.writeValueAsString(jsonTree(block))
}