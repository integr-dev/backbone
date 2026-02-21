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

package net.integr.backbone.commands

import net.integr.backbone.Backbone
import net.integr.backbone.commands.arguments.customItemArgument
import net.integr.backbone.commands.arguments.scriptArgument
import net.integr.backbone.commands.arguments.stringArgument
import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.Execution
import net.integr.backbone.systems.hotloader.ScriptEngine
import net.integr.backbone.systems.hotloader.ScriptLinker
import net.integr.backbone.systems.hotloader.ScriptStore
import net.integr.backbone.systems.item.ItemHandler
import net.integr.backbone.systems.text.component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import java.awt.Color

object BackboneCommand : Command("backbone", "Base command for Backbone", listOf("bb")) {
    override fun onBuild() {
        subCommands(Scripting, Item)
    }

    override suspend fun exec(ctx: Execution) {
        ctx.respond("Backbone v${Backbone.VERSION}")
    }

    object Scripting : Command("scripting", "Commands for Backbone scripting system") {
        val scriptingPerm = Backbone.ROOT_PERMISSION.derive("scripting")

        override fun onBuild() {
            subCommands(Reload, Enable, Disable, Wipe)
        }

        override suspend fun exec(ctx: Execution) {
            ctx.requirePermission(scriptingPerm)

            ctx.respond("Scripts [${ScriptStore.scripts.size}]:")
            for (script in ScriptStore.scripts) {
                ctx.respondComponent(component {
                    append("  - ${script.key.substringBefore(".bb.kts")}: ") {
                        color(Color(169, 173, 168))
                        onHover(HoverEvent.showText(component {
                            append(if (script.value.enabled) "Disable " else "Enable ")
                            append(script.key.substringBefore(".bb.kts"))
                        }))

                        onClick(ClickEvent.runCommand(
                            "bb scripting ${if (script.value.enabled) "disable" else "enable"} "
                                    + script.key.substringBefore(".bb.kts")
                        ))
                    }

                    append(if (script.value.enabled) "✔ enabled" else "❌ disabled") {
                        color(
                            if (script.value.enabled) Color(141, 184, 130)
                            else Color(201, 82, 60)
                        )
                    }
                })
            }
        }

        object Reload : Command("reload", "Reload all Backbone scripts") {
            val scriptingReloadPerm = scriptingPerm.derive("reload")

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(scriptingReloadPerm)

                ctx.respond("Reloading scripts...")
                val start = System.currentTimeMillis()

                val hasError = ScriptLinker.compileAndLink()
                val time = System.currentTimeMillis() - start
                ctx.respond("Scripts reloaded in ${time}ms.")
                if (hasError) ctx.fail("Some scripts failed to compile. See console for details.")
            }
        }

        object Enable : Command("enable", "Enable a Backbone script") {
            val scriptingEnablePerm = scriptingPerm.derive("enable")

            override fun onBuild() {
                arguments(
                    scriptArgument("script", "The script to enable")
                )
            }

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(scriptingEnablePerm)

                val script = ctx.get<String>("script")

                ctx.respond("Enabling script...")

                try {
                    ScriptEngine.enableScript(script)
                    ctx.respond("Script enabled.")
                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while enabling the script.")
                }
            }
        }

        object Disable : Command("disable", "Disable a Backbone script") {
            val scriptingDisablePerm = scriptingPerm.derive("disable")

            override fun onBuild() {
                arguments(
                    scriptArgument("script", "The script to disable")
                )
            }

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(scriptingDisablePerm)
                val script = ctx.get<String>("script")

                ctx.respond("Disabling script...")

                try {
                    ScriptEngine.disableScript(script)
                    ctx.respond("Script disabled.")
                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while disabling the script.")
                }
            }
        }

        object Wipe : Command("wipe", "Wipes the sustained state from a script") {
            val scriptingWipePerm = scriptingPerm.derive("wipe")

            override fun onBuild() {
                arguments(
                    scriptArgument("script", "The script to wipe state from"),
                    stringArgument("confirmation", "The name of the script for confirmation")
                )
            }

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(scriptingWipePerm)
                val script = ctx.get<String>("script")
                val confirmation = ctx.get<String>("confirmation")

                if (script != confirmation) {
                    ctx.fail("Confirmation failed. Script name does not match: $script != $confirmation")
                }

                ctx.respond("Wiping state from script...")

                try {
                    ScriptEngine.wipeScript(script)
                    ctx.respond("Script state wiped.")
                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while wiping state from the script.")
                }
            }
        }
    }

    object Item : Command("item", "Commands for Backbone item system") {
        val itemPerm = Backbone.ROOT_PERMISSION.derive("item")

        override fun onBuild() {
            subCommands(Give, Replicate, Read)
        }

        override suspend fun exec(ctx: Execution) {
            ctx.requirePermission(itemPerm)

            ctx.respond("Items [${ItemHandler.items.size}]:")
            for (item in ItemHandler.items) {
                ctx.respondComponent(component {
                    append("  - ${item.key}") {
                        color(Color(169, 173, 168))
                    }
                })
            }
        }

        object Give : Command("give", "Gives a custom item") {
            val itemGivePerm = itemPerm.derive("give")

            override fun onBuild() {
                arguments(
                    customItemArgument("item", "The custom item to give")
                )
            }

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(itemGivePerm)
                ctx.requirePlayer()

                val item = ctx.get<String>("item")

                ctx.respond("Generating item...")

                try {
                    val stack = ItemHandler.generate(item)
                    ctx.getPlayer().inventory.addItem(stack)
                    ctx.respond("Item generated.")
                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while generating the item.")
                }
            }
        }

        object Replicate : Command("replicate", "Copies the held item with a new instance.") {
            val itemReplicatePerm = itemPerm.derive("replicate")

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(itemReplicatePerm)
                ctx.requirePlayer()

                ctx.respond("Replicating item...")

                try {
                    val stack = ItemHandler.replicate(ctx.getPlayer().inventory.itemInMainHand)
                    if (stack == null) ctx.fail("Item is not a backbone item.")
                    else ctx.getPlayer().inventory.addItem(stack)
                    ctx.respond("Item replicated.")
                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while generating the item.")
                }
            }
        }

        object Read : Command("read", "Reads all meta tags from an item.") {
            val itemReadPerm = itemPerm.derive("read")

            override suspend fun exec(ctx: Execution) {
                ctx.requirePermission(itemReadPerm)
                ctx.requirePlayer()

                ctx.respond("Reading item...")

                try {
                    val tags = ItemHandler.readTags(ctx.getPlayer().inventory.itemInMainHand)

                    ctx.respond("Tags [${tags.size}]:")
                    for (tag in tags) {
                        ctx.respondComponent(component {
                            append("  - ${tag.key}: ") {
                                color(Color(169, 173, 168))
                            }

                            append(tag.value) {
                                color(Color(141, 184, 130))
                            }
                        })
                    }

                } catch (e: Exception) {
                    ctx.fail(e.message ?: "An error occurred while generating the item.")
                }
            }
        }
    }
}