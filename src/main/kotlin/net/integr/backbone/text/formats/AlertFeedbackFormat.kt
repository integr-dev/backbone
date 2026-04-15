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

package net.integr.backbone.text.formats

import net.integr.backbone.systems.text.component
import net.integr.backbone.text.alphabets.BoldSmallAlphabet
import net.kyori.adventure.text.Component
import java.awt.Color

/**
 * A class for formatting alert feedback messages with a consistent style, including a handler name and color.
 *
 * @param handler The name of the handler (e.g., plugin name) to display in the feedback.
 * @param handlerColor The color to use for the handler name.
 * @since 1.9.0
 */
class AlertFeedbackFormat(handler: String) {
    private val handlerName = BoldSmallAlphabet.encode(handler)

    /**
     * Format a string into a formatted error message component with the handler name and color.
     *
     * @param str The string to format.
     * @return A formatted [Component].
     * @since 1.9.0
     */
    fun formatError(str: String): Component {
        return component {
            append(handlerName) {
                color(Color(245, 66, 75))
            }

            append(" » ") {
                color(Color(43, 43, 42))
            }

            append("× $str") {
                color(Color(245, 66, 75))
            }
        }
    }

    /**
     * Format a string into a formatted warning message component with the handler name and color.
     *
     * @param str The string to format.
     * @return A formatted [Component].
     * @since 1.9.0
     */
    fun formatWarning(str: String): Component {
        return component {
            append(handlerName) {
                color(Color(255, 165, 0))
            }

            append(" » ") {
                color(Color(43, 43, 42))
            }

            append("⚡ $str") {
                color(Color(255, 165, 0))
            }
        }
    }
}