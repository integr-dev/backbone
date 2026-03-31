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

package net.integr.backbone.systems.hotloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.integr.backbone.Backbone
import net.integr.backbone.events.IscEvent
import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.entity.CustomEntity
import net.integr.backbone.systems.hotloader.isc.IscMap
import net.integr.backbone.systems.hotloader.isc.IscMapBuilder
import net.integr.backbone.systems.item.CustomItem
import net.integr.backbone.systems.network.http.HttpMethod
import net.integr.backbone.systems.network.http.Requestor
import net.integr.backbone.systems.network.http.request.RequestBuilder
import net.integr.backbone.systems.network.http.response.Response
import net.integr.backbone.systems.placeholder.PlaceholderGroup
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import kotlin.reflect.full.starProjectedType

/**
 * Makes an asynchronous HTTP request to the given URI with the specified method and request builder block.
 *
 * @param uri The target URI for the HTTP request.
 * @param method The HTTP method to use (e.g., GET, POST).
 * @param builderBlock An optional block to configure the request using a [RequestBuilder].
 * @return A [Response] containing the response data as a string.
 * @since 1.6.0
 */
suspend fun request(uri: String, method: HttpMethod, builderBlock: RequestBuilder.() -> Unit = {}) =
    Requestor.request(uri, method, builderBlock)

/**
 * Makes a synchronous HTTP request to the given URI with the specified method and request builder block.
 *
 * @param uri The target URI for the HTTP request.
 * @param method The HTTP method to use (e.g., GET, POST).
 * @param builderBlock An optional block to configure the request using a [RequestBuilder].
 * @return A [Response] containing the response data as a string.
 * @since 1.6.0
 */
fun requestSync(uri: String, method: HttpMethod, builderBlock: RequestBuilder.() -> Unit = {}) =
    Requestor.requestSync(uri, method, builderBlock)

/**
 * Makes an asynchronous HTTP request to the given URI with the specified method and request builder block, then executes a callback with the response.
 *
 * @param uri The target URI for the HTTP request.
 * @param method The HTTP method to use (e.g., GET, POST).
 * @param builderBlock An optional block to configure the request using a [RequestBuilder].
 * @param then A callback block that receives the [Response] containing the response data as a string.
 * @since 1.6.0
 */
fun requestAndThen(uri: String, method: HttpMethod, builderBlock: RequestBuilder.() -> Unit = {}, then: (Response<String>) -> Unit)
    = Requestor.requestAndThen(uri, method, builderBlock, then)

/**
 * Registers a Bukkit event listener for the given event type.
 *
 * @param priority The Bukkit event priority (default: NORMAL).
 * @param block The event handler block.
 * @since 1.6.0
 */
inline fun <reified T : org.bukkit.event.Event> LifecycleBuilder.listener(
    priority: org.bukkit.event.EventPriority = org.bukkit.event.EventPriority.NORMAL,
    noinline block: (T) -> Unit
) {
    onLoad { Backbone.SERVER.pluginManager.registerEvent(T::class.java, this, priority, { _, event -> if (event is T) block(event) }, Backbone.PLUGIN) }
    onUnload { Backbone.unregisterListener(this) }
}

/**
 * Registers a Backbone event listener for the given event type.
 *
 * @param priority The Backbone event priority (default: NORMAL).
 * @param block The event handler block.
 * @since 1.6.0
 */
inline fun <reified T : net.integr.backbone.systems.event.Event> LifecycleBuilder.backboneListener(
    priority: net.integr.backbone.systems.event.EventPriority = net.integr.backbone.systems.event.EventPriority.NORMAL,
    noinline block: (T) -> Unit
) {
    onLoad { Backbone.EVENT_BUS.registerLambda(T::class.starProjectedType, priority, this) { event -> if (event is T) block(event) } }
    onUnload { Backbone.unregisterListener(this) }
}

/**
 * Registers a handler for inter-script communication events with the given id.
 *
 * @param id The message id to listen for.
 * @param block The handler block, receiving the message data as [IscMap].
 * @since 1.6.0
 */
fun LifecycleBuilder.interScriptListener(id: String, block: (IscMap) -> Unit) {
    backboneListener<IscEvent> {
        if (it.id == id) block(it.data)
    }
}

/**
 * Dispatches an inter-script communication event with the given id and data.
 *
 * @param id The message id to send.
 * @param data The data builder block for the message.
 * @since 1.6.0
 */
fun dispatchInterScript(id: String, data: IscMapBuilder.() -> Unit) {
    val builder = IscMapBuilder()
    builder.data()
    val isc = builder.build()
    Backbone.EVENT_BUS.post(IscEvent(id, isc))
}

/**
 * Registers a command handler for the given command.
 *
 * @param command The command to register.
 * @since 1.6.0
 */
fun LifecycleBuilder.useCommand(command: Command) {
    onLoad { Backbone.Handler.COMMAND.register(command) }
    onUnload { Backbone.Handler.COMMAND.unregister(command) }
}

/**
 * Registers an entity handler for the given custom entity.
 *
 * @param entity The custom entity to register.
 * @since 1.6.0
 */
fun <T : Mob> LifecycleBuilder.useEntity(entity: CustomEntity<T>) {
    onLoad { Backbone.Handler.ENTITY.register(entity) }
    onUnload { Backbone.Handler.ENTITY.unregister(entity) }
}

/**
 * Registers an item handler for the given custom item.
 *
 * @param item The custom item to register.
 * @since 1.6.0
 */
fun LifecycleBuilder.useItem(item: CustomItem) {
    onLoad { Backbone.Handler.ITEM.register(item) }
    onUnload { Backbone.Handler.ITEM.unregister(item) }
}

/**
 * Registers a placeholder group with the given id, author, version, and block to define placeholders.
 *
 * @param id The unique identifier for the placeholder group.
 * @param author The author of the placeholder group.
 * @param version The version of the placeholder group.
 * @param block A block to define the placeholders within the group using a [PlaceholderGroup].
 * @since 1.7.1
 */
fun LifecycleBuilder.usePlaceholder(id: String, author: String, version: String, block: PlaceholderGroup.() -> Unit) {
    val group = PlaceholderGroup(id, author, version)
    group.block()

    onLoad { group.registerPlaceholders() }
    onUnload { group.unregisterPlaceholders() }
}