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

import java.net.URL
import java.net.URLClassLoader

class ExtendableClassLoader(parent: ClassLoader) : URLClassLoader(emptyArray(), parent) {
    private val extraClasses = mutableMapOf<String, ByteArray>()

    fun addClasses(entries: Map<String, ByteArray>) {
        extraClasses.putAll(entries)
    }

    public override fun addURL(url: URL) {
        super.addURL(url)
    }

    override fun findClass(name: String): Class<*> {
        val bytes = extraClasses[name.replace('.', '/') + ".class"]
        return if (bytes != null) {
            defineClass(name, bytes, 0, bytes.size)
        } else {
            super.findClass(name)
        }
    }
}