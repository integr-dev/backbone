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

import net.integr.backbone.systems.network.http.response.Response

/**
 * Represents a retry policy for HTTP requests, including max retries, delay, and retry condition.
 *
 * @property maxRetries The maximum number of retry attempts.
 * @property delayMillis The delay in milliseconds between retries.
 * @property retryOn The predicate to determine if a retry should occur based on the response.
 * @since 1.7.0
 */
data class RetryPolicy(
    val maxRetries: Int = 3,
    val delayMillis: Long = 500,
    val retryOn: (Response<*>) -> Boolean = { !it.ok }
)