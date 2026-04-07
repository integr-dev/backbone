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

import java.lang.ref.WeakReference

/**
 * Data class representing a probe for detecting potential memory leaks in hot-reloaded scripts.
 * Each probe tracks a specific script and epoch, along with weak references to the lifecycle instance
 * and classloader associated with that script. The probe also records the time it was created to allow
 * for age-based checks.
 *
 * @param script The identifier of the script being probed.
 * @param epoch The reload epoch associated with the script instance.
 * @param lifecycleRef A weak reference to the lifecycle instance of the script, used to check if it has been garbage collected.
 * @param classLoaderRef A weak reference to the classloader of the script, used to check if it has been garbage collected.
 * @param createdAtMs The timestamp (in milliseconds) when the probe was created, used for determining the age of the probe
 * @since 1.8.0
 */
data class LeakProbe(
    val script: String,
    val epoch: Long,
    val lifecycleRef: WeakReference<Any>,
    val classLoaderRef: WeakReference<ClassLoader>,
    val createdAtMs: Long = System.currentTimeMillis()
)