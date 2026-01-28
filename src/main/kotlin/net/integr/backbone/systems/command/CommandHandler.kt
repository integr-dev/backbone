package net.integr.backbone.systems.command

import net.integr.backbone.Backbone
import net.integr.backbone.systems.text.format.impl.CommandFeedbackFormat
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import java.lang.reflect.Field

object CommandHandler {
    val logger = Backbone.LOGGER.derive("command-handler")
    val feedbackFormat = CommandFeedbackFormat("backbone", "#8db882")

    private val map: CommandMap by lazy {
        logger.info("Got command map via reflection")
        val bukkitCommandMap: Field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
        bukkitCommandMap.isAccessible = true
        bukkitCommandMap.get(Bukkit.getServer()) as CommandMap
    }


    fun register(command: Command) {
        command.build()
        map.register(command.name, command)
    }
}