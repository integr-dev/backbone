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
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.jetbrains.annotations.ApiStatus
import java.awt.Color

/**
 *
 * Builds a Kyori Adventure Component using a DSL.
 *
 * Example usage:
 * ```kotlin
 * component {
 *     append("Hello") {
 *         color(Color.RED)
 *     }
 *     append("World") {
 *         color(Color.GREEN)
 *         onHover(HoverEvent.showText(component {
 *             append("Hover Text")
 *         }))
 *     }
 * }
 * ```
 *
 * @param block The DSL block to construct the component.
 * @return The built [Component].
 * @since 1.0.0
 */
fun component(block: ComponentBuilder.() -> Unit): Component {
    val builder = ComponentBuilder()
    builder.block()
    return builder.build()
}

/**
 *
 * A DSL builder for creating an Kyori Adventure [Component].
 * @since 1.0.0
 */
class ComponentBuilder {
    val base = Component.text()

    /**
     * Appends a text component with optional styling.
     *
     * @param text The text content of the component.
     * @param block An optional DSL block to apply styling to the component.
     * @since 1.0.0
     */
    fun append(text: String, block: ComponentStyleBuilder.() -> Unit = {}) {
        val style = ComponentStyleBuilder()
        style.block()
        base.append(Component.text(text, style.build()))
    }

    fun newLine() {
        base.append(Component.newline())
    }

    fun space() {
        base.append(Component.space())
    }

    fun append(component: Component) {
        base.append(component)
    }

    /**
     * Builds the final [Component] from the appended parts.
     *
     * @return The constructed [Component].
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun build(): Component {
        return base.build()
    }
}

/**
 * A DSL builder for creating a style for an  Kyori Adventure [Component].
 * @since 1.0.0
 */
class ComponentStyleBuilder {
    val base = Style.style()

    /**
     * Sets the color of the component.
     *
     * @param color The [TextColor] to apply.
     * @since 1.0.0
     */
    fun color(color: TextColor) {
        base.color(color)
    }

    /**
     *
     * Sets the color of the component using a [java.awt.Color].
     *
     * @param color The [java.awt.Color] to apply.
     * @since 1.0.0
     */
    fun color(color: Color) {
        base.color(TextColor.color(color.rgb))
    }

    /**
     * Sets the shadow color of the component.
     *
     * @param color The [ShadowColor] to apply.
     * @since 1.0.0
     */
    fun shadowColor(color: ShadowColor) {
        base.shadowColor(color)
    }

    /**
     * Decorates the component with the given [TextDecoration].
     *
     * @param decoration The [TextDecoration] to apply.
     * @since 1.0.0
     */
    fun decorate(decoration: TextDecoration) {
        base.decorate(decoration)
    }

    /**
     * Add a click event to the component.
     *
     * @param clickEvent The [ClickEvent] to apply.
     * @since 1.0.0
     */
    fun onClick(clickEvent: ClickEvent) {
        base.clickEvent(clickEvent)
    }

    /**
     * Add a HoverEvent to the component.
     *
     * @param hoverEvent The [HoverEvent] to apply.
     * @since 1.0.0
     */
    fun <T> onHover(hoverEvent: HoverEvent<T>) {
        base.hoverEvent(hoverEvent)
    }

    /**
     * Builds the final [Style] from the values.
     *
     * @return The constructed [Style].
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun build(): Style {
        return base.build()
    }
}
