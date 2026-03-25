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

package net.integr.backbone.systems.network.http.request

import java.net.http.HttpRequest
import tools.jackson.databind.ObjectMapper

/**
 * Builder for HTTP request bodies, supporting string, JSON, byte array, and multipart types.
 * Provides a DSL for multipart form data.
 *
 * @since 1.7.0
 */
class RequestBodyBuilder {
    private var bodyType: BodyType? = null
    private val multipartParts = mutableListOf<MultipartPart>()
    private var multipartBoundary: String? = null

    lateinit var publisher: HttpRequest.BodyPublisher
        private set

    /**
     * Sets the request body as a string.
     * @param content The string content.
     * @since 1.7.0
     */
    fun string(content: String) {
        setBodyType(BodyType.STRING)
        publisher = HttpRequest.BodyPublishers.ofString(content)
    }

    /**
     * Sets the request body as JSON, serializing the given object.
     * @param obj The object to serialize as JSON.
     * @since 1.7.0
     */
    fun json(obj: Any) {
        setBodyType(BodyType.JSON)
        val json = ObjectMapper().writeValueAsString(obj)
        publisher = HttpRequest.BodyPublishers.ofString(json)
    }

    /**
     * Sets the request body as a byte array.
     * @param bytes The byte array content.
     * @since 1.7.0
     */
    fun bytes(bytes: ByteArray) {
        setBodyType(BodyType.BYTES)
        publisher = HttpRequest.BodyPublishers.ofByteArray(bytes)
    }

    /**
     * Sets the request body as multipart form data using a DSL block.
     * @param block The multipart DSL block.
     * @since 1.7.0
     */
    fun multipart(block: MultipartDsl.() -> Unit) {
        setBodyType(BodyType.MULTIPART)
        val dsl = MultipartDsl()
        dsl.block()
        multipartParts.clear()
        multipartParts.addAll(dsl.parts)
        multipartBoundary = dsl.boundary
        publisher = buildMultipartPublisher()
    }

    private fun setBodyType(type: BodyType) {
        if (bodyType != null && bodyType != type) {
            throw IllegalStateException("Only one body type can be set per request.")
        }
        bodyType = type
    }

    private fun buildMultipartPublisher(): HttpRequest.BodyPublisher {
        val boundary = multipartBoundary ?: "----BackboneBoundary${System.currentTimeMillis()}"
        val byteArrays = mutableListOf<ByteArray>()
        for (part in multipartParts) {
            when (part) {
                is MultipartPart.Field -> {
                    byteArrays += "--$boundary\r\n".toByteArray()
                    byteArrays += "Content-Disposition: form-data; name=\"${part.name}\"\r\n\r\n".toByteArray()
                    byteArrays += part.value.toByteArray()
                    byteArrays += "\r\n".toByteArray()
                }
                is MultipartPart.File -> {
                    byteArrays += "--$boundary\r\n".toByteArray()
                    byteArrays += "Content-Disposition: form-data; name=\"${part.name}\"; filename=\"${part.filename}\"\r\n".toByteArray()
                    if (part.contentType != null) {
                        byteArrays += "Content-Type: ${part.contentType}\r\n".toByteArray()
                    }
                    byteArrays += "\r\n".toByteArray()
                    byteArrays += part.content
                    byteArrays += "\r\n".toByteArray()
                }
            }
        }
        byteArrays += "--$boundary--\r\n".toByteArray()
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays)
    }

    /**
     * DSL for building multipart request bodies.
     * @since 1.7.0
     */
    class MultipartDsl {
        internal val parts = mutableListOf<MultipartPart>()
        internal var boundary: String? = null

        /**
         * Adds a form field part to the multipart body.
         * @param name The field name.
         * @param value The field value.
         * @since 1.7.0
         */
        fun field(name: String, value: String) {
            parts += MultipartPart.Field(name, value)
        }
        /**
         * Adds a file part to the multipart body.
         * @param name The field name.
         * @param filename The file name.
         * @param content The file content as bytes.
         * @param contentType The file content type (optional).
         * @since 1.7.0
         */
        fun file(name: String, filename: String, content: ByteArray, contentType: String? = null) {
            parts += MultipartPart.File(name, filename, content, contentType)
        }
        /**
         * Sets a custom boundary for the multipart body.
         * @param boundary The boundary string.
         * @since 1.7.0
         */
        fun boundary(boundary: String) {
            this.boundary = boundary
        }
    }

    /**
     * Represents a part of a multipart request body.
     * @since 1.7.0
     */
    sealed class MultipartPart {
        /**
         * Represents a form field part in multipart data.
         * @property name The field name.
         * @property value The field value.
         * @since 1.7.0
         */
        data class Field(val name: String, val value: String) : MultipartPart()
        /**
         * Represents a file part in multipart data.
         * @property name The field name.
         * @property filename The file name.
         * @property content The file content as bytes.
         * @property contentType The file content type (optional).
         * @since 1.7.0
         */
        data class File(val name: String, val filename: String, val content: ByteArray, val contentType: String?) : MultipartPart() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as File

                if (name != other.name) return false
                if (filename != other.filename) return false
                if (!content.contentEquals(other.content)) return false
                if (contentType != other.contentType) return false

                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + filename.hashCode()
                result = 31 * result + content.contentHashCode()
                result = 31 * result + (contentType?.hashCode() ?: 0)
                return result
            }
        }
    }

    private enum class BodyType { STRING, JSON, BYTES, MULTIPART }
}
