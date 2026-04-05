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
import kotlinx.coroutines.launch
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
import net.integr.backbone.systems.update.UpdateChecker
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.ApiStatus
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.time.measureTime

/**
 * The main plugin class for the Backbone API.
 * @since 1.0.0
 */
@ApiStatus.Internal
class BackboneServer : JavaPlugin() {
    private var tickTask: BukkitTask? = null
    private var beforeOut: PrintStream? = null
    private var beforeErr: PrintStream? = null

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    override fun onEnable() {
        val timeTaken = measureTime {
            val out = PrintStream(FileOutputStream(FileDescriptor.out), true)
            val err = PrintStream(FileOutputStream(FileDescriptor.err), true)

            beforeOut = System.out
            beforeErr = System.err

            // Bypass papers println intercept
            System.setOut(out)
            System.setErr(err)

            Backbone.SCRIPT_POOL.create()

            tickTask = Backbone.dispatchMainTimer(0L, 1L) {
                EventBus.post(TickEvent())
            }

            runBlocking { // Perform initialization tasks in parallel using coroutines to speed up startup time
                launch { // Misc initialization
                    BStatHandler.init()

                    Backbone.registerListener(GuiHandler)
                    Backbone.registerListener(ItemHandler)
                    Backbone.registerListener(EntityHandler)

                    Backbone.Handler.COMMAND.register(BackboneCommand)
                }

                launch { // Compile and link scripts
                    val scriptsTimeTaken = measureTime {
                        ScriptLinker.compileAndLink()
                    }

                    Backbone.LOGGER.info("Compiled and linked scripts in ${scriptsTimeTaken.inWholeSeconds}s")
                }

                launch { // Set up placeholders
                    setPlaceholders()

                    Backbone.PLACEHOLDER_GROUP.registerPlaceholders()
                }

                launch(Dispatchers.IO) { // Check for updates
                    if (Backbone.CONFIG_STATE.checkForUpdates) {
                        UpdateChecker.checkUpdate()
                    }
                }

                Backbone.LOGGER.info("Dispatched initialization tasks, waiting for completion...")
            }
        }

        Backbone.LOGGER.info("Initialization tasks completed in ${timeTaken.inWholeSeconds}s, backbone is now enabled!")
    }

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    override fun onDisable() {
        val timeTaken = measureTime {
            tickTask?.cancel() // Stop the tick task

            Backbone.PLACEHOLDER_GROUP.unregisterPlaceholders()

            runBlocking {
                launch {
                    BStatHandler.shutdown()

                    Backbone.unregisterListener(GuiHandler)
                    Backbone.unregisterListener(ItemHandler)
                    Backbone.unregisterListener(EntityHandler)

                    Backbone.Handler.COMMAND.unregister(BackboneCommand)
                }

                launch {
                    ScriptEngine.unloadScripts()
                }

                Backbone.LOGGER.info("Dispatched shutdown tasks, waiting for completion...")
            }

            // Restore original System.out and System.err
            beforeOut?.let { System.setOut(it) }
            beforeErr?.let { System.setErr(it) }
        }

        Backbone.LOGGER.info("Shutdown tasks completed in ${timeTaken.inWholeMilliseconds}ms, backbone is now disabled!")

    }

    /**
     * Sets placeholders for the plugin.
     * @since 1.0.0
     */
    private fun setPlaceholders() {
        Backbone.PLACEHOLDER_GROUP.add("version") { _, _ ->
            return@add Backbone.VERSION
        }
    }
}