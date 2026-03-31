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

package net.integr.backbone

import kotlinx.coroutines.runBlocking
import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.bstats.BStatHandler
import net.integr.backbone.systems.entity.EntityHandler
import net.integr.backbone.systems.event.EventBus
import net.integr.backbone.systems.gui.GuiHandler
import net.integr.backbone.systems.hotloader.ScriptEngine
import net.integr.backbone.systems.hotloader.ScriptLinker
import net.integr.backbone.systems.item.ItemHandler
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * The main plugin class for the Backbone API.
 * @since 1.0.0
 */
@ApiStatus.Internal
class BackboneServer : JavaPlugin() {
    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    override fun onEnable() {
        val fdOut = FileOutputStream(FileDescriptor.out)
        val originalOut = PrintStream(fdOut, true)

        // Bypass papers println intercept
        System.setOut(originalOut)
        System.setErr(PrintStream(FileOutputStream(FileDescriptor.err), true))

        Backbone.SCRIPT_POOL.create()

        runBlocking {
            ScriptLinker.compileAndLink()
        }

        BStatHandler.init()

        Backbone.registerListener(GuiHandler)
        Backbone.registerListener(ItemHandler)
        Backbone.registerListener(EntityHandler)

        Backbone.Handler.COMMAND.register(BackboneCommand)

        Backbone.dispatchMainTimer(0L, 1L) {
            EventBus.post(TickEvent())
        }

        Backbone.dispatchMain {
            setPlaceholders()
        }
    }

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    override fun onDisable() {
        runBlocking {
            ScriptEngine.unloadScripts() // Cleanup
        }
    }

    /**
     * Sets placeholders for the plugin.
     * @since 1.0.0
     */
    private fun setPlaceholders() {
        Backbone.PLACEHOLDER_GROUP.add("version") { _, _ ->
            return@add Backbone.VERSION
        }

        Backbone.PLACEHOLDER_GROUP.registerPlaceholders()
    }
}