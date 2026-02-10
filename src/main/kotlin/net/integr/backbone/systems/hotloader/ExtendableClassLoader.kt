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