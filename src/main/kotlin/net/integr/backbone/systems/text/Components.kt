package net.integr.backbone.systems.text

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.ComponentStyleBuilder

fun component(block: ComponentBuilder.() -> Unit): BaseComponent {
    val builder = ComponentBuilder()
    builder.block()
    return builder.build()
}

fun ComponentBuilder.style(block: ComponentStyleBuilder.() -> Unit): ComponentBuilder {
    val builder = ComponentStyleBuilder()
    builder.block()
    this.style(builder.build())
    return this
}

fun ComponentBuilder.append(text: String, block: ComponentStyleBuilder.() -> Unit) {
    this.append(text, ComponentBuilder.FormatRetention.NONE)
    this.style(block)
}