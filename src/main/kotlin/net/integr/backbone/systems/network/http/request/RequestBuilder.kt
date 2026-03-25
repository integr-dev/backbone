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

import net.integr.backbone.systems.network.http.response.Response
import net.integr.backbone.systems.network.http.RetryPolicy
import net.integr.backbone.systems.network.http.HttpMethod
import net.integr.backbone.systems.network.http.Requestor
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.time.Duration
import java.util.Base64

/**
 * Builder for HTTP requests, supporting headers, authentication, query parameters, cookies, timeouts, retry policies, and logging.
 * Provides a DSL for configuring requests.
 *
 * @property client The HTTP client to use for the request.
 * @property retryPolicy The retry policy for the request.
 * @property logger The logger for request/response events.
 * @since 1.7.0
 */
class RequestBuilder internal constructor(private var uri: String, private val method: HttpMethod) {
    private val headers = mutableListOf<Pair<String, String>>()
    private var bodyPublisher: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody()
    private val queryParams = mutableListOf<Pair<String, String>>()
    private var timeoutMillis: Long? = null
    private var cookies = mutableMapOf<String, String>()

    var client: HttpClient = DEFAULT_CLIENT
        private set
    var retryPolicy: RetryPolicy? = null
        private set

    var logger: ((HttpRequest, Response<*>) -> Unit)? = null
        private set

    /**
     * Sets a header for the request.
     * @param name The header name.
     * @param value The header value.
     * @since 1.7.0
     */
    fun header(name: String, value: String) = apply { headers += name to value }

    /**
     * Sets a Bearer authentication header.
     * @param token The bearer token.
     * @since 1.7.0
     */
    fun bearer(token: String) = header("Authorization", "Bearer $token")

    /**
     * Sets a Basic authentication header.
     * @param username The username.
     * @param password The password.
     * @since 1.7.0
     */
    fun basic(username: String, password: String) = header(
        "Authorization",
        "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    )

    /**
     * Adds a query parameter to the request URI.
     * @param name The parameter name.
     * @param value The parameter value.
     * @since 1.7.0
     */
    fun query(name: String, value: String) = apply { queryParams += name to value }

    /**
     * Adds a cookie to the request.
     * @param name The cookie name.
     * @param value The cookie value.
     * @since 1.7.0
     */
    fun cookie(name: String, value: String) = apply { cookies[name] = value }

    /**
     * Sets a timeout for the request in milliseconds.
     * @param millis The timeout in milliseconds.
     * @since 1.7.0
     */
    fun timeout(millis: Long) = apply { timeoutMillis = millis }

    /**
     * Sets the HTTP client to use for the request.
     * @param client The HttpClient instance.
     * @since 1.7.0
     */
    fun client(client: HttpClient) = apply { this.client = client }

    /**
     * Sets the retry policy for the request.
     * @param retryPolicy The RetryPolicy instance.
     * @since 1.7.0
     */
    fun retry(retryPolicy: RetryPolicy) = apply { this.retryPolicy = retryPolicy }

    /**
     * Sets a logger for request/response events.
     * @param logger The logger function.
     * @since 1.7.0
     */
    fun log(logger: (HttpRequest, Response<*>) -> Unit) = apply { this.logger = logger }

    /**
     * Sets the request body using a builder DSL.
     * @param block The body builder block.
     * @since 1.7.0
     */
    fun body(block: RequestBodyBuilder.() -> Unit) = apply {
        val dsl = RequestBodyBuilder()
        dsl.block()
        bodyPublisher = dsl.publisher
    }

    /**
     * Builds the HttpRequest from the configured properties.
     * @return The built HttpRequest.
     * @since 1.7.0
     */
    fun build(): HttpRequest {
        val finalUri = if (queryParams.isNotEmpty()) {
            val base = URI(uri)
            val existing = base.query?.let { "$it&" } ?: ""

            val qp = queryParams.joinToString("&") { (k, v) ->
                URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
            }

            URI(base.scheme, base.authority, base.path, existing + qp, base.fragment).toString()
        } else uri

        val builder = HttpRequest.newBuilder().uri(URI.create(finalUri))

        headers.forEach { (k, v) -> builder.header(k, v) }

        if (cookies.isNotEmpty()) {
            val cookieHeader = cookies.entries.joinToString("; ") { (k, v) -> "$k=$v" }
            builder.header("Cookie", cookieHeader)
        }

        if (timeoutMillis != null) builder.timeout(Duration.ofMillis(timeoutMillis!!))

        when (method) {
            HttpMethod.GET -> builder.GET()
            HttpMethod.POST -> builder.POST(bodyPublisher)
            HttpMethod.PUT -> builder.PUT(bodyPublisher)
            HttpMethod.DELETE -> builder.DELETE()
            HttpMethod.PATCH -> builder.method("PATCH", bodyPublisher)
            HttpMethod.HEAD -> builder.method("HEAD", HttpRequest.BodyPublishers.noBody())
            HttpMethod.OPTIONS -> builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            HttpMethod.TRACE -> builder.method("TRACE", HttpRequest.BodyPublishers.noBody())
        }

        return builder.build()
    }

    companion object {
        /**
         * The default HTTP client used for requests.
         * @since 1.7.0
         */
        val DEFAULT_CLIENT: HttpClient = HttpClient.newHttpClient()
    }
}
