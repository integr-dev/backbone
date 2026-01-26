package net.integr.backbone.base

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.Execution
import net.integr.backbone.systems.command.arguments.impl.IntegerArgument
import net.integr.backbone.systems.command.arguments.impl.PlayerArgument
import net.integr.backbone.systems.command.arguments.impl.StringArgument
import org.bukkit.entity.Player

object BackboneCommand : Command("backbone", "Base command for Backbone", listOf("bb")) {
    override fun onBuild() {
        sub(Reload)

        argument(StringArgument("name", "Name of the object"))
        argument(IntegerArgument("count", "Count of the object"))
    }

    override fun exec(ctx: Execution) {
        val name = ctx.get<String>("name") ?: throw IllegalArgumentException("Name argument is required")
        val count = ctx.get<Int>("count") ?: 0

        if (count == 2) ctx.fail("Count cannot be 2")

        ctx.respond("Backbone command executed with name: '$name' and count: '$count'")
    }

    object Reload : Command("reload", "Reloads the Backbone configuration") {
        override fun onBuild() {
            sub(ClearCache)

            argument(IntegerArgument("level", "Level of reload"))
        }

        override fun exec(ctx: Execution) {
            ctx.failOnPlayer()
            val level = ctx.get<Int>("level") ?: 2
            logger.info("Reloading Backbone with level $level")

            ctx.respond("Backbone reloaded with level $level")
        }

        object ClearCache : Command("clearcache", "Clears the Backbone cache") {
            override fun onBuild() {
                argument(StringArgument("cacheType", "Type of cache to clear"))
                argument(PlayerArgument("player", "Player whose cache to clear"))
            }

            override fun exec(ctx: Execution) {
                logger.info("Clearing Backbone cache")
                ctx.respond("Player: ${ctx.get<Player>("player")?.name}, Cache Type: ${ctx.get<String>("cacheType")}")
                ctx.respond("Backbone cache cleared")
            }
        }
    }

}