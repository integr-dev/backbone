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

package net.integr.backbone.systems.update

import net.integr.backbone.Backbone
import net.integr.backbone.systems.network.http.HttpMethod
import net.integr.backbone.systems.network.http.Requestor

object UpdateChecker {
    private val logger = Backbone.LOGGER.derive("update-checker")
    private const val UPDATE_URL = "https://api.modrinth.com/v2/project/backbone-lib/version?include_changelog=false"

    suspend fun hasUpdate(): Boolean { //TODO add config to disable update checks, also use the checker
        val response = Requestor.request(UPDATE_URL, HttpMethod.GET)

        if (response.ok) {
            val body = response.json()
            val latestVersion = body[0]["name"].stringValue()
            val currentVersion = Backbone.VERSION

            return isNewerVersion(latestVersion, currentVersion)
        }

        logger.warning("Failed to check for updates: ${response.code} ${response.body}")
        return false
    }

    fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val latestPart = latestParts.getOrNull(i) ?: 0

            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }

        return false
    }
}