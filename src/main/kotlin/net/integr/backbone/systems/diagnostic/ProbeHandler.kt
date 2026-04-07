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

package net.integr.backbone.systems.diagnostic

import net.integr.backbone.Backbone
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.set

object ProbeHandler {
    val logger = Backbone.LOGGER.derive("probe-handler")

    private val probes = ConcurrentHashMap<String, LeakProbe>() // key: "$script:$epoch"
    private val failedChecks = ConcurrentHashMap<String, Int>()

    fun register(script: String, epoch: Long, oldLifecycle: Any) {
        val key = "$script:$epoch"
        probes[key] = LeakProbe(
            script = script,
            epoch = epoch,
            lifecycleRef = WeakReference(oldLifecycle),
            classLoaderRef = WeakReference(oldLifecycle.javaClass.classLoader)
        )
    }

    fun check(nowMs: Long = System.currentTimeMillis()) {
        for ((key, probe) in probes) {
            val ageMs = nowMs - probe.createdAtMs
            if (ageMs < 60_000) continue // grace period after reload

            val lifecycleAlive = probe.lifecycleRef.get() != null
            val loaderAlive = probe.classLoaderRef.get() != null

            if (!lifecycleAlive && !loaderAlive) {
                probes.remove(key)
                failedChecks.remove(key)
                continue
            }

            val n = (failedChecks[key] ?: 0) + 1
            failedChecks[key] = n

            if (n >= 3) {
                // escalate only after repeated failures to avoid false positives
                logger.warning("Possible script leak: script=${probe.script}, epoch=${probe.epoch}, lifecycleAlive=$lifecycleAlive, classLoaderAlive=$loaderAlive, ageMs=$ageMs")
            }
        }
    }
}