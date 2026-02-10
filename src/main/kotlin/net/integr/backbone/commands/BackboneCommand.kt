package net.integr.backbone.commands

import net.integr.backbone.Backbone
import net.integr.backbone.commands.arguments.scriptArgument
import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.Execution
import net.integr.backbone.systems.hotloader.ScriptEngine
import net.integr.backbone.systems.hotloader.ScriptLinker
import net.integr.backbone.systems.hotloader.ScriptStore

object BackboneCommand : Command("backbone", "Base command for Backbone", listOf("bb")) {
    override fun onBuild() {
        subCommands(Scripting)
    }

    override suspend fun exec(ctx: Execution) {
        ctx.respond("Backbone v${Backbone.VERSION}")
    }

    object Scripting : Command("scripting", "Commands for Backbone scripting system") {
        val scriptingPerm = Backbone.ROOT_PERMISSION.derive("scripting")

        override fun onBuild() {
            subCommands(Reload, Enable, Disable)
        }

        override suspend fun exec(ctx: Execution) {
            ctx.requirePermission(scriptingPerm)

            ctx.respond("Enabled scripts: ${ScriptStore.getEnabledScripts().joinToString(", ")}") //TODO: better formatting here
            ctx.respond("Disabled scripts: ${ScriptStore.getDisabledScripts().joinToString(", ")}")
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
    }
}