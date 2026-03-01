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

import org.jetbrains.annotations.ApiStatus
import java.net.URL
import java.net.URLClassLoader

/**
 * A custom [URLClassLoader] that allows adding new classes and URLs dynamically.
 *
 * This class is used by the hot-reloading system to load compiled script classes and their dependencies
 * without restarting the entire application. It extends [URLClassLoader] to provide methods for
 * adding new class definitions as byte arrays and new URLs to the classpath.
 *
 * @param parent The parent class loader.
 * @since 1.0.0
 */
@ApiStatus.Internal
class ExtendableClassLoader(parent: ClassLoader) : URLClassLoader(emptyArray(), parent) {
    private val extraClasses = mutableMapOf<String, ByteArray>()

    /**
     *
     * Adds a map of class names to their byte array definitions to this class loader.
     * These classes can then be loaded dynamically.
     *
     * @param entries A map where keys are fully qualified class names (e.g., "com.example.MyClass")
     *                and values are the byte arrays representing the class definitions.
     * @since 1.0.0
     */
    fun addClasses(entries: Map<String, ByteArray>) {
        extraClasses.putAll(entries)
    }

    /**
     * Adds a URL to the search path for classes and resources.
     *
     * @param url The URL to add.
     * @since 1.0.0
     */
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