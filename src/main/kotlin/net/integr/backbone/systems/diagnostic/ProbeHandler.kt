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
import org.jetbrains.annotations.ApiStatus
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * A utility object for tracking potential memory leaks of script lifecycles and classloaders after hot-reloads.
 *
 * This object maintains weak references to the lifecycle instances and their classloaders associated with each script reload.
 * It periodically checks if these references have been garbage collected, which would indicate that the old script has been fully unloaded. If references remain alive for an extended period after a reload, it logs warnings to help identify potential memory leaks in the hot-reloading process.
 *
 * @since 1.8.0
 */
object ProbeHandler {
    private val logger = Backbone.LOGGER.derive("probe-handler")
    private const val GRACE_PERIOD_MS = 60_000L
    private const val GC_HINT_COOLDOWN_MS = 90_000L
    private const val GC_HINT_AFTER_FAILED_CHECKS = 2
    private const val WARN_AFTER_FAILED_CHECKS = 3
    private var lastGcHintMs = 0L

    /**
     * All active probes tracking script reloads, keyed by a combination of script name and reload epoch. Each probe contains weak references to the lifecycle instance and classloader of the old script, allowing us to detect if they have been garbage collected.
     *
     * The key format is "$script:$epoch", where `script` is the name of the script being reloaded and `epoch` is the reload epoch associated with that reload. This allows us to track multiple reloads of the same script over time.
     *
     * @since 1.8.0
     */
    @ApiStatus.Internal
    val probes = ConcurrentHashMap<String, LeakProbe>() // key: "$script:$epoch"

    private val failedChecks = ConcurrentHashMap<String, Int>()

    /**
     * Registers a new probe for a script reload, tracking the lifecycle and classloader of the old script instance.
     *
     * @param script The name of the script being reloaded.
     * @param epoch The reload epoch associated with this reload.
     * @param oldLifecycle The lifecycle instance of the old script, which may be leaked if not properly unloaded.
     * @param classLoader The classloader that loaded the old script. This must be explicitly passed to ensure
     *                     the probe tracks the correct classloader independent of the lifecycle object's lifecycle.
     *
     * @since 1.8.0
     */
    fun register(script: String, epoch: Long, oldLifecycle: Any, classLoader: ClassLoader) {
        val key = "$script:$epoch"
        probes[key] = LeakProbe(
            script = script,
            epoch = epoch,
            lifecycleRef = WeakReference(oldLifecycle),
            classLoaderRef = WeakReference(classLoader)
        )
    }

    /**
     * Checks all registered probes for potential leaks by verifying if the lifecycle and classloader references have been garbage collected. If references remain alive for an extended period after a reload, it logs warnings to help identify potential memory leaks.
     *
     * @param nowMs The current time in milliseconds, used to calculate the age of each probe. Defaults to the current system time.
     * @return A list of probes that are suspected to be leaking based on their lifecycle and classloader references still being alive after multiple checks.
     * @since 1.8.0
     */
    fun check(nowMs: Long = System.currentTimeMillis()): List<LeakProbe> {
        val leaks: MutableList<LeakProbe> = mutableListOf()
        var shouldHintGc = false

        for ((key, probe) in probes) {
            val ageMs = nowMs - probe.createdAtMs
            if (ageMs < GRACE_PERIOD_MS) continue // grace period after reload

            val lifecycleAlive = probe.lifecycleRef.get() != null
            val loaderAlive = probe.classLoaderRef.get() != null

            if (!lifecycleAlive && !loaderAlive) {
                probes.remove(key)
                failedChecks.remove(key)
                continue
            }

            val n = (failedChecks[key] ?: 0) + 1
            failedChecks[key] = n

            if (n >= GC_HINT_AFTER_FAILED_CHECKS) {
                shouldHintGc = true
            }

            if (n >= WARN_AFTER_FAILED_CHECKS) {
                // escalate only after repeated failures to avoid false positives
                logger.warning("Possible script leak: script=${probe.script}, epoch=${probe.epoch}, lifecycleAlive=$lifecycleAlive, classLoaderAlive=$loaderAlive, ageMs=$ageMs")
                leaks.add(probe)
            }
        }

        // Hint GC sparingly to avoid stalling the server thread while still reducing weak-ref false positives.
        if (shouldHintGc && nowMs - lastGcHintMs >= GC_HINT_COOLDOWN_MS) {
            logger.info("Hinting GC to collect potential script leaks...")
            System.gc()
            lastGcHintMs = nowMs
        }

        return leaks
    }
}