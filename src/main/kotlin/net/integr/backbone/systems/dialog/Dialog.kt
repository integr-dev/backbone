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


@file:Suppress("UnstableApiUsage")

package net.integr.backbone.systems.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.RegistryBuilderFactory
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.integr.backbone.Utils
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import java.util.function.Consumer


/**
 * Builds a Paper [Dialog] using a DSL.
 *
 * Example usage:
 * ```kotlin
 * dialog {
 *     base(component { append("My Dialog") }) {
 *         // configure base properties
 *     }
 *     type(myDialogType)
 * }
 * ```
 *
 * @param block The DSL block to construct the dialog.
 * @return The built [Dialog].
 * @since 1.4.0
 */
fun dialog(block: DialogBuilder.() -> Unit): Dialog {
    val con = Consumer { builder: RegistryBuilderFactory<Dialog, out DialogRegistryEntry.Builder> ->
        val db = DialogBuilder(builder.empty())
        db.block()
    }

    return Dialog.create(con)
}

/**
 * A DSL builder for creating a Paper [Dialog].
 *
 * @param builder The underlying [DialogRegistryEntry.Builder] used to construct the dialog.
 * @since 1.4.0
 */
class DialogBuilder(val builder: DialogRegistryEntry.Builder) {
    /**
     * Sets the base configuration of the dialog, including its title and additional properties.
     *
     * @param title The title [Component] of the dialog.
     * @param block A DSL block to configure the [DialogBase.Builder].
     * @since 1.4.0
     */
    fun base(title: Component, block: DialogBase.Builder.() -> Unit) {
        val res = Utils.blockBuild<DialogBase.Builder, DialogBase>(DialogBase.builder(title), block)
        builder.base(res)
    }

    /**
     * Sets the type of the dialog.
     *
     * @param type The [DialogType] to use for this dialog.
     * @since 1.4.0
     */
    fun type(type: DialogType) {
        builder.type(type)
    }
}

/**
 * Creates a custom click [DialogAction] with the given options and callback.
 *
 * The callback is invoked when the user interacts with the dialog action,
 * providing the [DialogResponseView] and the [Audience] that triggered the action.
 *
 * @param options The [ClickCallback.Options] to configure the click behavior.
 * @param callback The callback to execute when the action is triggered.
 * @return A [DialogAction] representing the custom click action.
 * @since 1.4.0
 */
fun customClick(
    options: ClickCallback.Options,
    callback: (response: DialogResponseView, audience: Audience) -> Unit
): DialogAction {
    return DialogAction.customClick(callback, options)
}