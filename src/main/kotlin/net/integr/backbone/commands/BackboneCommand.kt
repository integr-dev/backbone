package net.integr.backbone.commands

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.Execution
import net.integr.backbone.systems.command.arguments.impl.booleanArgument
import net.integr.backbone.systems.command.arguments.impl.enumArgument
import net.integr.backbone.systems.command.arguments.impl.integerArgument
import net.integr.backbone.systems.command.arguments.impl.playerArgument
import net.integr.backbone.systems.command.arguments.impl.stringArgument
import net.integr.backbone.systems.storage.ResourcePool
import org.bukkit.entity.Player

object BackboneCommand : Command("backbone", "Base command for Backbone", listOf("bb")) {
    enum class MyEnum {
        ONE, TWO, THREE
    }

    val pool = ResourcePool.fromStorage("backbone")
    val db = pool.database("database.db")

    override fun onBuild() {
        subCommands(Reload)

        stringArgument("name", "Name of the object")
        integerArgument("count", "Count of the object")
        enumArgument<MyEnum>("enum", "Enum of the object")
        booleanArgument("is-a", "If the object is a")
    }

    override fun exec(ctx: Execution) {
        val name = ctx.get<String>("name") ?: throw IllegalArgumentException("Name argument is required")
        val count = ctx.get<Int>("count") ?: 0
        val isA = ctx.get<Boolean>("is-a") ?: false

        if (count == 2) ctx.fail("Count cannot be 2")

        ctx.respond("Backbone command executed with name: '$name' and count: '$count'")
        ctx.respond(isA.toString())

        db.useConnection { connection, savepoint ->
            val changed = usePreparedStatement("INSERT INTO test (id, name) VALUES (?id, ?name);") { statement ->
                statement.setInt(0, 2)
                statement.setString(1, "test")
                val changed = statement.executeUpdate()

                return@usePreparedStatement changed
            }

            if (changed == 0) {
                connection.rollback(savepoint)
                ctx.respond("Rollback")
                return@useConnection
            }

            useStatement { statement ->
                val res = statement.executeQuery("SELECT * FROM test;")
                while (res.next()) {
                    ctx.respond(res.getString("name"))
                }
            }
        }
    }

    object Reload : Command("reload", "Reloads the Backbone configuration") {
        override fun onBuild() {
            subCommands(ClearCache)

            integerArgument("level", "Level of reload")
        }

        override fun exec(ctx: Execution) {
            ctx.failOnPlayer()
            val level = ctx.get<Int>("level") ?: 2
            logger.info("Reloading Backbone with level $level")

            ctx.respond("Backbone reloaded with level $level")
        }

        object ClearCache : Command("clear-cache", "Clears the Backbone cache") {
            override fun onBuild() {
                stringArgument("cache-type", "Type of cache to clear")
                playerArgument("player", "Player whose cache to clear")
            }

            override fun exec(ctx: Execution) {
                logger.info("Clearing Backbone cache")
                ctx.respond("Player: ${ctx.get<Player>("player")?.name}, Cache Type: ${ctx.get<String>("cache-type")}")
                ctx.respond("Backbone cache cleared")
            }
        }
    }

}