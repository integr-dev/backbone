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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.events.TickEvent
import net.integr.backbone.items.TestItem
import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.event.EventBus
import net.integr.backbone.systems.gui.GuiHandler
import net.integr.backbone.systems.hotloader.ScriptEngine
import net.integr.backbone.systems.hotloader.ScriptLinker
import net.integr.backbone.systems.item.ItemHandler
import org.bukkit.plugin.java.JavaPlugin

class BackboneServer : JavaPlugin() {
    override fun onEnable() {
        Backbone.LOGGER.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)

        Backbone.SCRIPT_POOL.create()

        setPlaceholders()

        runBlocking {
            async(Dispatchers.IO) {
                ScriptLinker.compileAndLink()
            }
        }

        Backbone.registerListener(GuiHandler)
        Backbone.registerListener(ItemHandler)

        ItemHandler.register(TestItem)

        Backbone.SCHEDULER.runTaskTimer(Backbone.PLUGIN, Runnable {
            EventBus.post(TickEvent)
        }, 0L, 1L)

    }

    override fun onDisable() {
        Backbone.LOGGER.info("Shutting down Backbone")

        runBlocking {
            async(Dispatchers.IO) {
                ScriptEngine.unloadScripts() // Cleanup
            }
        }
    }

    fun setPlaceholders() {
        Backbone.PLACEHOLDER_GROUP.create("backbone_version") { _, _ ->
            return@create Backbone.VERSION
        }

        Backbone.PLACEHOLDER_GROUP.registerAll()
    }
}