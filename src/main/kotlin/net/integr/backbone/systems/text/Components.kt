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

package net.integr.backbone.systems.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import java.awt.Color

fun component(block: ComponentBuilder.() -> Unit): Component {
    val builder = ComponentBuilder()
    builder.block()
    return builder.build()
}


class ComponentBuilder {
    private val base = Component.text()

    fun append(text: String, block: ComponentStyleBuilder.() -> Unit = {}) {
        val style = ComponentStyleBuilder()
        style.block()
        base.append(Component.text(text, style.build()))
    }

    fun build(): Component {
        return base.build()
    }
}

class ComponentStyleBuilder {
    val base = Style.style()

    fun color(color: TextColor) {
        base.color(color)
    }

    fun color(color: Color) {
        base.color(TextColor.color(color.rgb))
    }

    fun onClick(clickEvent: ClickEvent) {
        base.clickEvent(clickEvent)
    }

    fun <T> onHover(hoverEvent: HoverEvent<T>) {
        base.hoverEvent(hoverEvent)
    }

    fun build(): Style {
        return base.build()
    }
}
