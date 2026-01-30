package net.integr.backbone.commands

import kotlinx.serialization.Serializable
import net.integr.backbone.Backbone
import net.integr.backbone.guis.TestGui
import net.integr.backbone.systems.async.AsyncHandler
import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.Execution
import net.integr.backbone.commands.arguments.completedArgument
import net.integr.backbone.commands.arguments.enumArgument
import net.integr.backbone.commands.arguments.integerArgument
import net.integr.backbone.commands.arguments.validatedArgument
import net.integr.backbone.commands.arguments.stringArgument

object BackboneCommand : Command("backbone", "Base command for Backbone", listOf("bb")) {
    enum class MyEnum {
        ONE, TWO, THREE
    }

    @Serializable
    data class MyConfig(val name: String, val age: String)

    val db = Backbone.STORAGE_POOL.database("database.db")
    val cfg = Backbone.CONFIG_POOL.config<MyConfig>("config.yaml")

    override fun onBuild() {
        subCommands(Reload, ShowGui)

        arguments(
            stringArgument("name", "Name of the object"),
            integerArgument("count", "Count of the object"),
            validatedArgument(completedArgument(stringArgument("my-str", "abc"), listOf("abc", "def", "ghi"))) { el ->
                if (el.length > 3) return@validatedArgument fail("must be less than 2 characters long")
                else return@validatedArgument ok()
            }
        )
    }

    override fun exec(ctx: Execution) {
        val name = ctx.get<String>("name") ?: throw IllegalArgumentException("Name argument is required")
        val count = ctx.get<Int>("count") ?: 0

        if (count == 2) ctx.fail("Count cannot be 2")

        ctx.respond("Backbone command executed with name: '$name' and count: '$count'")

        val future = AsyncHandler.runAsync {
            return@runAsync db.useConnection { connection, savepoint ->
                execute("CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT)")

                val changed = preparedStatement("INSERT INTO test (id, name) VALUES (?, ?)") {
                    setInt(1, count)
                    setString(2, name)
                    val changed = executeUpdate()

                    return@preparedStatement changed
                }

                if (changed == 0 || changed == null) {
                    connection.rollback(savepoint)
                    return@useConnection null
                }

                return@useConnection query<List<String>>("SELECT * FROM test") { rs ->
                    val results = mutableListOf<String>()

                    while (rs.next()) {
                        results.add(rs.getString("name"))
                    }

                    return@query results
                }
            }
        }

        future.thenAccept { res ->
            if (res == null) {
                ctx.respond("Database operation failed.")
                return@thenAccept
            }

            ctx.respond("Database operation succeeded. Names in DB: ${res.joinToString(", ")}")
        }
    }

    object ShowGui : Command("show-gui", "Shows a test GUI") {
        override fun exec(ctx: Execution) {
            ctx.failOnConsole()
            ctx.requirePermission(Backbone.ROOT_PERMISSION.derive("show-gui"))

            TestGui.open(ctx.getPlayer())
            ctx.respond("Test GUI opened")
        }
    }

    object Reload : Command("reload", "Reloads the Backbone configuration") {
        override fun onBuild() {
            subCommands(ClearCache)

            arguments(
                integerArgument("level", "Level of reload")
            )
        }

        override fun exec(ctx: Execution) {
            ctx.failOnPlayer()
            ctx.requirePermission(Backbone.ROOT_PERMISSION.derive("reload"))
            val level = ctx.get<Int>("level") ?: 2
            logger.info("Reloading Backbone with level $level")

            ctx.respond("Backbone reloaded with level $level")
        }

        object ClearCache : Command("clear-cache", "Clears the Backbone cache") {
            override fun onBuild() {
                arguments(
                    stringArgument("cache-type", "Type of cache to clear"),
                    enumArgument<MyEnum>("enum", "Enum of the object")
                )
            }

            override fun exec(ctx: Execution) {
                logger.info("Clearing Backbone cache")
                ctx.respond("Backbone cache cleared")

                cfg.writeState(MyConfig("test", "test"))
            }
        }
    }

}