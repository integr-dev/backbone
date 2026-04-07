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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.isActive
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that dispatches tasks to the main server thread.
 *
 * This dispatcher checks if the current thread is the primary server thread. If it is, it runs the task immediately.
 * Otherwise, it schedules the task to run on the main server thread using Bukkit's scheduler.
 *
 * @since 1.7.2
 */
class ServerDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) {
            return
        }

        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Backbone.SERVER.scheduler.runTask(Backbone.PLUGIN, block)
        }
    }
}

/**
 * Extension function to get an instance of [ServerDispatcher].
 *
 * @return An instance of [ServerDispatcher] that can be used to dispatch tasks to the main server thread.
 * @since 1.7.2
 */
fun Dispatchers.serverDispatcher(): CoroutineDispatcher = ServerDispatcher()