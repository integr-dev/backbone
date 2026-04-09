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
 * Creates a [DiagnosticResult] representing a successful diagnostic operation, containing the value and any diagnostics.
 *
 * @param T The type of the value being diagnosed.
 * @param diagnostics A list of diagnostics generated during the operation, which may include warnings or errors
 * @return A [DiagnosticResult] with success set to true and the provided diagnostics.
 *
 * @since 1.8.1
 */
fun <T> T.diagnosticSuccess(diagnostics: List<Diagnostic> = emptyList()): DiagnosticResult<T> {
    return DiagnosticResult(this, true, diagnostics)
}

/**
 * Creates a [DiagnosticResult] representing a failed diagnostic operation, containing the value and any diagnostics.
 *
 * @param T The type of the value being diagnosed.
 * @param diagnostics A list of diagnostics generated during the operation, which may include warnings or errors
 * @return A [DiagnosticResult] with success set to false and the provided diagnostics.
 *
 * @since 1.8.1
 */
fun <T> T.diagnosticFailure(diagnostics: List<Diagnostic> = emptyList()): DiagnosticResult<T> {
    return DiagnosticResult(this, false, diagnostics)
}

/**
 * A wrapper for the result of a diagnostic operation, containing the value, success status, and any diagnostics.
 *
 * @param T The type of the value being diagnosed.
 * @property value The value resulting from the diagnostic operation.
 * @property success Whether the diagnostic operation was successful.
 * @property diagnostics A list of diagnostics generated during the operation, which may include warnings or errors
 *
 * @since 1.8.1
 */
data class DiagnosticResult<T>(val value: T, val success: Boolean, val diagnostics: List<Diagnostic>)