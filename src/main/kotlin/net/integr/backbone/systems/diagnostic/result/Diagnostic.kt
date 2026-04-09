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

package net.integr.backbone.systems.diagnostic.result

/**
 * Represents a diagnostic message generated during an operation, including its severity level.
 *
 * @property message The content of the diagnostic message.
 * @property severity The severity level of the diagnostic, indicating its importance or impact.
 *
 * @since 1.8.1
 */
data class Diagnostic(val message: String, val severity: Severity) {
    enum class Severity {
        INFO, WARNING, ERROR
    }
}

fun diagnosticList(): MutableList<Diagnostic> {
    return mutableListOf()
}