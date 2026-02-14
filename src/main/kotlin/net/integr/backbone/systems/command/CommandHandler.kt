package net.integr.backbone.systems.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.integr.backbone.Backbone
import net.integr.backbone.text.formats.CommandFeedbackFormat
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import java.awt.Color
import java.lang.reflect.Field


object CommandHandler {
    val logger = Backbone.LOGGER.derive("command-handler")
    val defaultFeedbackFormat = CommandFeedbackFormat("backbone", Color(141, 184, 130))

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val map: CommandMap by lazy {
        logger.info("Got command map via reflection.")
        val bukkitCommandMap: Field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
        bukkitCommandMap.isAccessible = true
        bukkitCommandMap.get(Bukkit.getServer()) as CommandMap
    }


    fun register(command: Command, prefix: String = "backbone") {
        command.build()
        map.register(prefix, command)
        Bukkit.getOnlinePlayers().forEach {
            it.updateCommands()
        }
    }

    fun unregister(command: Command, prefix: String = "backbone") {
        unregisterCommand(command.name, prefix)

        Bukkit.getOnlinePlayers().forEach {
            it.updateCommands()
        }
    }

    fun unregisterCommand(commandName: String, prefix: String = "backbone") {
        try {
            val knownCommandsField = map.javaClass.getSuperclass().getDeclaredField("knownCommands")
            knownCommandsField.setAccessible(true)
            val knownCommands = knownCommandsField.get(map) as MutableMap<*, *>

            knownCommands.remove(commandName)
            knownCommands.remove("$prefix:$commandName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}