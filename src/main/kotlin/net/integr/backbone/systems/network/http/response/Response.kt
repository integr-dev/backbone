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

package net.integr.backbone.systems.network.http.response

import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.net.http.HttpResponse

/**
 * Represents an HTTP response, wrapping status code, body, headers, URI, and the original HttpResponse.
 * Provides convenience properties for status and JSON parsing, and mapping to objects.
 *
 * @param T The type of the response body.
 * @property code The HTTP status code.
 * @property body The response body as a string.
 * @property headers The response headers.
 * @property uri The request URI as a string.
 * @property base The original HttpResponse object.
 * @since 1.7.0
 */
data class Response<T>(
    val code: Int,
    val body: String,
    val headers: Map<String, List<String>>,
    val uri: String,
    val base: HttpResponse<T>
) {
    val ok: Boolean get() = code in 200..299
    fun json(): JsonNode = mapper.readTree(body)

    inline fun <reified R> mapJson(): R = mapper.readValue(this.body)

    fun <R> mapJson(mapper: (String) -> R): R = mapper(this.body)

    companion object {
        val mapper = ObjectMapper()
        /**
         * Creates a Response from a standard HttpResponse.
         * @param response The HttpResponse to wrap.
         * @return The wrapped Response.
         * @since 1.7.0
         */
        fun <T> from(response: HttpResponse<T>): Response<T> {
            return Response(
                code = response.statusCode(),
                body = response.body().toString(),
                headers = response.headers().map(),
                uri = response.uri().toString(),
                base = response
            )
        }
    }
}