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

import net.integr.backbone.systems.text.append
import net.integr.backbone.systems.text.component
import net.integr.backbone.text.alphabets.BoldSmallAlphabet
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import java.awt.Color

class CommandFeedbackFormat(handler: String, val handlerColor: Color) {
    private val handlerName = BoldSmallAlphabet.encode(handler)

    fun format(str: String): BaseComponent {
        return component {
            append(handlerName) {
                color(ChatColor.of(handlerColor))
            }

            append(" » ") {
                color(ChatColor.of(Color(43, 43, 42)))
            }

            append(str) {
                color(ChatColor.of(Color(169, 173, 168)))
            }
        }
    }

    fun formatErr(str: String): BaseComponent {
        return component {
            append(handlerName) {
                color(ChatColor.of(handlerColor))
            }

            append(" » ") {
                color(ChatColor.of(Color(43, 43, 42)))
            }

            append(str) {
                color(ChatColor.of(Color(245, 66, 75)))
            }
        }
    }
}