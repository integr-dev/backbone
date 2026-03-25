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

package net.integr.backbone.systems.network.http

import kotlinx.coroutines.CoroutineScope
import java.net.http.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.integr.backbone.systems.network.http.request.RequestBuilder
import net.integr.backbone.systems.network.http.response.Response

/**
 * A utility for wrapping network requests for easier status and response handling.
 * Supports a DSL for headers, authentication, and retry policies.
 *
 * @since 1.7.0
 */
object Requestor {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    /**
     * Performs an HTTP request with the given URI, method, and builder block.
     * Handles retries and logging as configured in the builder.
     *
     * @param uri The request URI.
     * @param method The HTTP method to use.
     * @param builderBlock The configuration block for the request builder.
     * @return The HTTP response wrapped in a Response object.
     * @since 1.7.0
     */
    suspend fun request(
        uri: String,
        method: HttpMethod,
        builderBlock: RequestBuilder.() -> Unit = {}
    ): Response<String> = withContext(Dispatchers.IO) {
        val builder = RequestBuilder(uri, method)
        builder.builderBlock()
        val request = builder.build()

        val client = builder.client
        val retryPolicy = builder.retryPolicy
        val logger = builder.logger

        var lastResponse: Response<String>?
        var attempt = 0

        do {
            val res = client.send(request, HttpResponse.BodyHandlers.ofString())
            val response = Response.from(res)

            logger?.invoke(request, response)
            lastResponse = response

            if (retryPolicy == null || attempt >= retryPolicy.maxRetries || !retryPolicy.retryOn(response)) break

            Thread.sleep(retryPolicy.delayMillis)
            attempt++
        } while (true)

        lastResponse
    }

    /**
     * Synchronously performs an HTTP request. This is a blocking call and should be used with caution.
     *
     * @param uri The request URI.
     * @param method The HTTP method to use.
     * @param builderBlock The configuration block for the request builder.
     * @return The HTTP response wrapped in a Response object.
     * @since 1.7.0
     */
    fun requestSync(
        uri: String,
        method: HttpMethod,
        builderBlock: RequestBuilder.() -> Unit = {}
    ): Response<String> = runBlocking {
        request(uri, method, builderBlock)
    }

    /**
     * Performs an HTTP request and then executes a callback with the response. This is asynchronous and non-blocking.
     *
     * @param uri The request URI.
     * @param method The HTTP method to use.
     * @param builderBlock The configuration block for the request builder.
     * @param then The callback to execute with the response once the request is complete.
     * @since 1.7.0
     */
    fun requestAndThen(
        uri: String,
        method: HttpMethod,
        builderBlock: RequestBuilder.() -> Unit = {},
        then: (Response<String>) -> Unit
    ) {
        coroutineScope.launch {
            val response = request(uri, method, builderBlock)
            then(response)
        }
    }
}