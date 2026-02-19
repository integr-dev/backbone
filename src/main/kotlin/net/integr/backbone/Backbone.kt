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

import net.integr.backbone.systems.event.EventBus
import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.systems.placeholder.PlaceholderGroup
import net.integr.backbone.systems.storage.ResourcePool
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

object Backbone {
    //TODO: add entity helper
    private val executionContext = System.getenv("EXEC_CONTEXT")

    val STORAGE_POOL = ResourcePool.fromStorage("backbone")
    val CONFIG_POOL = ResourcePool.fromConfig("backbone")

    val SCRIPT_POOL = ResourcePool.getScripts()

    val ROOT_PERMISSION by lazy {
        PermissionNode("backbone")
    }

    val PLACEHOLDER_GROUP by lazy {
        PlaceholderGroup(VERSION, "backbone")
    }

    val VERSION by lazy {
        PLUGIN.description.version
    }

    private val pluginInternal: JavaPlugin? by lazy {
        try {
            JavaPlugin.getPlugin(BackboneServer::class.java)
        } catch (e: Exception) {
            null
        }
    }

    val PLUGIN
        get() = pluginInternal!!

    val LOGGER: BackboneLogger by lazy {
        BackboneLogger("backbone", if (executionContext != "test") pluginInternal else null)
    }

    val SCHEDULER by lazy {
        Bukkit.getScheduler()
    }

    fun registerListener(listener: Listener) {
        PLUGIN.server.pluginManager.registerEvents(listener, PLUGIN)
        EventBus.register(listener)
    }

    fun unregisterListener(listener: Listener) {
        HandlerList.unregisterAll(listener)
        EventBus.unRegister(listener)
    }

    fun dispatchMain(block: () -> Unit) {
        SCHEDULER.runTask(PLUGIN, block)
    }
}

