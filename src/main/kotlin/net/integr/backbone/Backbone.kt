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

import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.entity.EntityHandler
import net.integr.backbone.systems.event.EventBus
import net.integr.backbone.systems.gui.GuiHandler
import net.integr.backbone.systems.item.ItemHandler
import net.integr.backbone.systems.logger.BackboneLogger
import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.systems.placeholder.PlaceholderGroup
import net.integr.backbone.systems.storage.ResourcePool
import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.ApiStatus

/**
 *
 * The main entry point for the Backbone API.
 *
 * This object provides access to various Backbone systems and utilities.
 *
 * @since 1.0.0
 */
object Backbone {
    /**
     * Backbones internal storage pool. **Important:** Do not use this.
     * Create a new pool instead:
     * ```kotlin
     * val pool = ResourcePool.fromStorage("my-server")
     * ```
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val STORAGE_POOL = ResourcePool.fromStorage("backbone")

    /**
     * Backbones internal config pool. **Important:** Do not use this.
     * Create a new pool instead:
     * ```kotlin
     * val pool = ResourcePool.fromConfig("my-server")
     * ```
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val CONFIG_POOL = ResourcePool.fromConfig("backbone")

    /**
     * Backbones main config. **Important:** Do not use this.
     * Create a new config instead:
     * ```kotlin
     * val config = pool.config<MyConfig>("my-config.yaml")
     * ```
     *
     * @since 1.8.0
     */
    @get:ApiStatus.Internal
    val MAIN_CONFIG by lazy {
        CONFIG_POOL.config("backbone.yaml", BackboneConfig())
    }

    val CONFIG_STATE
        get() = MAIN_CONFIG.getState()!!

    /**
     * Internal script pool.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val SCRIPT_POOL = ResourcePool.getScripts()

    /**
     * Backbones internal logger. **Important:** Do not use this for your own logging.
     * Create a new logger instead:
     * ```kotlin
     * val logger = BackboneLogger("my-server")
     * ```
     *
     * @since 1.0.0
     */
    @get:ApiStatus.Internal
    val LOGGER by lazy {
        BackboneLogger("backbone", MAIN_CONFIG.getState()!!.loggerCompatibilityMode, pluginInternal)
    }

    /**
     * Backbones root permission. **Important:** Do not use this for your own permission checks.
     * Create a new node instead:
     * ```kotlin
     * val node = PermissionNode("my-server")
     * ```
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val ROOT_PERMISSION = PermissionNode("backbone")

    /**
     * Backbones placeholders. **Important:** Do not use this for your own custom placeholders.
     * Create a new group instead:
     * ```kotlin
     * val group = PlaceholderGroup("1.0.0", "my-server")
     * ```
     *
     * @since 1.0.0
     */
    @get:ApiStatus.Internal
    val PLACEHOLDER_GROUP by lazy {
        PlaceholderGroup("backbone", "Integr", VERSION)
    }

    /**
     * The internally checked plugin instance.
     * Null if we are not in a plugin environment.
     *
     * This is used to avoid null checks in the codebase.
     * If you are not in a plugin environment, you can safely ignore this.
     *
     * @since 1.0.0
     */
    @get:ApiStatus.Internal
    internal val pluginInternal: JavaPlugin? by lazy {
        Utils.tryOrNull { JavaPlugin.getPlugin(BackboneServer::class.java) } // For testing purposes we allow null here
    }

    /**
     * The global backbone plugin instance.
     *
     * @since 1.0.0
     */
    val PLUGIN
        get() = pluginInternal!!

    /**
     * The global server instance.
     *
     * @since 1.0.0
     */
    val SERVER
        get() = PLUGIN.server

    /**
     * The version of the backbone plugin.
     *
     * @since 1.0.0
     */
    val VERSION
        get() = PLUGIN.pluginMeta.version

    /**
     * The global event bus instance.
     *
     * @since 1.6.0
     */
    val EVENT_BUS
        get() = EventBus


    /**
     * Registers a listener to the server's plugin manager and the internal event bus.
     * 1. Registers the listener with the internal event bus.
     * 2. Registers the listener with the plugin manager.
     *
     * @param listener The listener to register.
     *
     * @since 1.0.0
     */
    fun registerListener(listener: Listener) {
        EventBus.register(listener)
        SERVER.pluginManager.registerEvents(listener, PLUGIN)
    }

    /**
     * Removes a listener from the server's plugin manager and the internal event bus.
     *
     * @param listener The listener to unregister.
     *
     * @since 1.0.0
     */
    fun unregisterListener(listener: Listener) {
        EventBus.unregister(listener)
        HandlerList.unregisterAll(listener)
    }

    /**
     * Dispatches a task to be run on the main thread of the server.
     *
     * @param block The block of code to run.
     *
     * @since 1.0.0
     */
    fun dispatchMain(block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTask(PLUGIN, block)
    }

    /**
     * Dispatches a task to be run asynchronously on the server.
     *
     * @param block The block of code to run.
     *
     * @since 1.0.0
     */
    fun dispatch(block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTaskAsynchronously(PLUGIN, block)
    }

    /**
     * Dispatches a task to be run asynchronously on the server after a delay.
     *
     * @param delay The delay in ticks before the task is run.
     * @param block The block of code to run.
     *
     * @since 1.7.1
     */
    fun dispatchLater(delay: Long, block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTaskLaterAsynchronously(PLUGIN, block, delay)
    }

    /**
     * Dispatches a task to be run asynchronously on the server repeatedly with a fixed delay between each run.
     *
     * @param delay The delay in ticks before the first run of the task.
     * @param period The delay in ticks between each run of the task.
     * @param block The block of code to run.
     *
     * @since 1.7.1
     */
    fun dispatchTimer(delay: Long, period: Long, block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTaskTimerAsynchronously(PLUGIN, block, delay, period)
    }

    /**
     * Dispatches a task to be run on the main thread of the server after a delay.
     *
     * @param delay The delay in ticks before the task is run.
     * @param block The block of code to run.
     *
     * @since 1.7.1
     */
    fun dispatchMainLater(delay: Long, block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTaskLater(PLUGIN, block, delay)
    }

    /**
     * Dispatches a task to be run on the main thread of the server repeatedly with a fixed delay between each run.
     *
     * @param delay The delay in ticks before the first run of the task.
     * @param period The delay in ticks between each run of the task.
     * @param block The block of code to run.
     *
     * @since 1.7.1
     */
    fun dispatchMainTimer(delay: Long, period: Long, block: () -> Unit): BukkitTask {
        return SERVER.scheduler.runTaskTimer(PLUGIN, block, delay, period)
    }

    /**
     * Gets a namespaced key.
     *
     * @param namespace The namespace of the key.
     * @param key The key.
     * @return A namespaced key.
     *
     * @since 1.0.0
     */
    fun getKey(namespace: String, key: String): NamespacedKey {
        return NamespacedKey(namespace, key)
    }

    /**
     * Backbone's handlers for use in your scripts.
     *
     * @since 1.1.0
     */
    object Handler {
        /**
         * The command handler used to register and manage commands.
         *
         * @since 1.1.0
         */
        val COMMAND
            get() = CommandHandler

        /**
         * The item handler used to manage items.
         *
         * @since 1.1.0
         */
        val ITEM
            get() = ItemHandler

        /**
         * The entity handler used to manage entities.
         *
         * @since 1.1.0
         */
        val ENTITY
            get() = EntityHandler

        /**
         * The gui handler used to manage guis.
         *
         * @since 1.1.0
         */
        val GUI
            get() = GuiHandler
    }
}

