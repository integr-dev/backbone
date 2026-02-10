package net.integr.backbone.systems.hotloader

import net.integr.backbone.Backbone

object ScriptEngine {
    val logger = Backbone.LOGGER.derive("script-engine")

    fun unloadScripts(): Boolean {
        var errs = false

        for ((name, state) in ScriptStore.scripts) {
            try {
                state.lifecycle.onUnload()
                state.enabled = false
                logger.info("Unloading script: $name")
            } catch (e: Exception) {
                logger.severe("Failed to unload script: $name")
                e.printStackTrace()
                errs = true
            }
        }

        ScriptStore.scripts.clear()
        return errs
    }

    fun disableScript(name: String) {
        val state = ScriptStore.getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (!state.enabled) {
            logger.warning("Script '$name' is already disabled.")
            throw IllegalArgumentException("Script '$name' is already disabled.")
        }

        try {
            script.onUnload()
            state.enabled = false
            logger.info("Disabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to disable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while disabling script.")
        }
    }

    fun enableScript(name: String) {
        val state = ScriptStore.getScriptByName(name) ?: throw IllegalArgumentException("Script not found.")
        val script = state.lifecycle

        if (state.enabled) {
            logger.warning("Script '$name' is already enabled.")
            throw IllegalArgumentException("Script '$name' is already enabled.")
        }

        try {
            script.onLoad()
            state.enabled = true
            logger.info("Enabled script: '$name'")
        } catch (e: Exception) {
            logger.severe("Failed to enable script: '$name'")
            e.printStackTrace()
            throw IllegalArgumentException("Exception occurred while enabling script.")
        }
    }
}