# Backbone

Backbone is a powerful and flexible plugin for Spigot-based Minecraft servers, designed to supercharge server customization. Its core philosophy is to enable server administrators and developers to write, test, and update server logic on a live server without requiring restarts, dramatically accelerating the development lifecycle.

Whether you're a server administrator looking to add custom features with simple scripts or a developer prototyping new ideas, Backbone provides the tools you need to be more productive and creative.

## Features

- **Hot-Loadable Scripts:** Write and reload Kotlin scripts without restarting the server, enabling rapid development and iteration.
- **Advanced Scripting:** Go beyond simple scripts with support for inter-script imports, Maven dependencies, and custom compiler options.
- **Event System:** A custom event bus that complements Bukkit's event system, offering more control and flexibility within your scripts.
- **Command Framework:** A simple yet powerful command system to create custom commands directly from your scripts.
- **Storage Abstraction:** Easily manage data with a flexible storage system that supports SQLite databases and typed configuration files.
- **GUI Framework:** A declarative GUI framework for creating complex and interactive inventories from your scripts.
- **Text Formatting:** A flexible text formatting system with support for custom alphabets and color codes.

## Getting Started

Getting started with Backbone is simple. The primary way to use Backbone is by installing it as a plugin and then creating your own custom features through its scripting engine.

1.  **Download:** Download the latest release from the [official releases page](https://github.com/integr-dev/backbone/releases).
2.  **Install:** Place the downloaded `.jar` file into your server's `plugins` directory.
3.  **Start:** Launch your server. Backbone will generate its necessary folders in your server's root directory.
4.  **Scripting:** You can now begin writing custom logic in `.bb.kts` script files inside the `scripts/` directory. See the examples below to get started!

For advanced users who wish to build a plugin that depends on Backbone, you can add it as a dependency. However, for most use cases, the scripting engine provides all the power you need.

## Examples

All examples are designed to be placed in their own `.bb.kts` files inside the `scripts/` directory.

### Hot-Loadable Scripts

Backbone's most powerful feature is its hot-loadable script engine. This allows you to write Kotlin code in script files (`.bb.kts`) and load, unload, and reload them on the fly without needing to restart your server. This is incredibly useful for rapid development, prototyping, and live server updates.

#### Script Location and Aggregation

All script files should be placed in the `scripts/` directory in your server's root. The script engine automatically discovers and compiles any `.bb.kts` files in this location when scripts are loaded.

#### Script Structure

Every script file must evaluate to an object that extends `ManagedLifecycle`. This class provides the necessary hooks for the script engine to manage the script's lifecycle.

```kotlin
import net.integr.backbone.Backbone
import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.event.BackboneEventHandler
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import net.integr.backbone.systems.hotloader.lifecycle.sustained

// Each script must return an object that extends ManagedLifecycle.
object : ManagedLifecycle() {
    // 'sustained' properties persist their values across script reloads.
    var counter by sustained(0)
    // Standard variables are reset every time the script is reloaded.
    var otherCounter = 0

    // Called when the script is loaded or enabled
    override fun onLoad() {
        Backbone.registerListener(this)
    }

    // Called when the script is unloaded or disabled
    override fun onUnload() {
        Backbone.unregisterListener(this)
    }

    // This event fires every server tick while the script is enabled.
    @BackboneEventHandler
    fun onTick(event: TickEvent) {
        counter++
        otherCounter++
        if (counter % 20 == 0) {
            Backbone.PLUGIN.server.onlinePlayers
                .forEach { it.sendMessage("Sustained Count: $counter | Volatile Count: $otherCounter") }
        }
    }
}
```

#### Managing Scripts

Backbone provides a set of commands to manage your scripts. The base command is `/backbone`, which can be aliased to `/bb`.

-   `/bb scripting`: Lists all discovered scripts and shows their current status (enabled/disabled).
-   `/bb scripting reload`: Unloads all current scripts, then loads and enables all scripts from the `scripts` directory. This is the primary command for applying changes.
-   `/bb scripting enable <script_name>`: Enables a specific script that is currently disabled.
-   `/bb scripting disable <script_name>`: Disables a specific script that is currently enabled.

### Advanced Scripting

You can make your scripts even more powerful by using file-level annotations to manage dependencies and compiler settings.

#### Sharing Code Between Scripts
You can define utility scripts with the `.bbu.kts` file extension. These scripts function as shared libraries. Backbone automatically compiles them and injects their classes and functions into the classpath and default imports of all main `.bb.kts` scripts.

**`utils.bbu.kts`**
```kotlin
class MyUtilities {
    fun getGreeting(): String = "Hello from a utility script!"
}
```
This means you can use the defined classes and methods in your main scripts as if they were in the same file.

**`main.bb.kts`**
```kotlin
// ... inside your ManagedLifecycle
val utils = MyUtilities()
println(utils.getGreeting()) // Prints "Hello from a utility script!"
```

#### Managing Dependencies with `@DependsOn` and `@Repository`

You can pull in external libraries directly from Maven repositories using the `@DependsOn` and `@Repository` annotations. This lets you use powerful third-party libraries without having to manually bundle them with your server.

```kotlin
// Add a custom Maven repository
@file:Repository("https://jitpack.io")
// Depend on a library from that repository
@file:DependsOn("com.github.javafaker:javafaker:1.0.2")

import com.github.javafaker.Faker
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository

// ... inside your script
val faker = Faker()
val randomName = faker.name().fullName()
println("A random name: $randomName")
```

#### Customizing with `@CompilerOptions`

The `@CompilerOptions` annotation gives you fine-grained control over the Kotlin compiler, allowing you to enable specific language features or pass other flags.

```kotlin
// Enable a specific language feature, like context receivers, or just pass any plain old compiler option
@file:CompilerOptions("-Xcontext-receivers")

// Your script code can now use context receivers
context(String)
fun greet() {
    println("Hello, $this")
}
```

### Storage and Configuration

Backbone provides a simple and powerful way to manage your plugin's data and configuration through `ResourcePool`s. This system allows you to easily handle databases and configuration files in a structured manner.

#### Resource Pools

A `ResourcePool` is a namespaced container for your resources. It's recommended to create a separate pool for each script or feature set to avoid conflicts.

```kotlin
// Create a resource pool for your script's storage
val myScriptStorage = ResourcePool.fromStorage("mystorage")

// Create a resource pool for your script's configuration
val myScriptConfig = ResourcePool.fromConfig("myconfig")
```

This will create directories at `storage/mystorage/` and `config/myconfig/` in your server's root directory.

#### Configuration

You can easily manage typed configuration files. Backbone handles the serialization and deserialization of your data classes automatically.

First, define a serializable data class for your configuration:

```kotlin
@Serializable // Requires the kotlinx.serialization plugin
data class MyConfig(val settingA: String = "default", val settingB: Int = 10)
```

Then, use the `config()` function on your resource pool to get a handler for it:

```kotlin
// Get a handler for a config file named 'settings.json'
val configHandler = myScriptConfig.config<MyConfig>("settings.json")

// Get the current config, or the default if it doesn't exist
val currentConfig = configHandler.get()
println("Setting A: ${currentConfig.settingA}")

// Modify and save the config
configHandler.set(currentConfig.copy(settingB = 20))
```

#### Databases

Backbone provides a simple and efficient way to work with SQLite databases from within your scripts.

```kotlin
// Get a connection to a database file named 'playerdata.db'
val dbConnection = myScriptStorage.database("playerdata.db")

// The useConnection block handles connection setup and teardown, preventing resource leaks.
dbConnection.useConnection {
    // The 'this' context is a StatementCreator instance.
    execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, name TEXT NOT NULL);")

    // Use a prepared statement to safely insert data.
    preparedStatement("INSERT INTO players (uuid, name) VALUES (?, ?)") {
        setString(1, "some-uuid")
        setString(2, "PlayerName")
        executeUpdate()
    }

    val playerName = query("SELECT name FROM players WHERE uuid = 'some-uuid'") { it.getString("name") }
    println("Found player: $playerName")
}
```

### Custom Events

Backbone's event system allows you to create and listen for custom events, giving you more control over your script's behavior.

```kotlin
// Define a custom event
class MyCustomEvent(val message: String) : Event()

// Register a listener for the custom event.
// Priority ranges from -3 to 3, with 0 being normal. Lower values execute first.
@BackboneEventHandler(EventPriority.THREE_BEFORE)
fun onMyCustomEvent(event: MyCustomEvent) {
    println("Received custom event: ${event.message}")
}

// Fire the custom event from anywhere in your code
EventBus.post(MyCustomEvent("Hello, world!"))
```

### Commands

Backbone's command framework makes it easy to create and manage commands with arguments, subcommands, and permission checks.

Commands are executed asynchronously by default. For this reason, any API calls that modify server state must be wrapped in a `Backbone.dispatchMain {}` block to ensure they run on the main server thread.

```kotlin
// Define a command
object MyCommand : Command("mycommand", "My first command") {
    val perm = PermissionNode("myplugin")

    override fun onBuild() {
        // Register subcommands
        subCommands(MySubCommand)

        // Define arguments for the command
        arguments(
            scriptArgument("text", "My string")
        )
    }

    override suspend fun exec(ctx: Execution) {
        // Require a permission for this command
        ctx.requirePermission(perm.derive("mycommand")) // "myplugin.mycommand"

        val text = ctx.get<String>("text")

        ctx.respond("Hello ${ctx.sender.name}: $text")

        // To affect server state, dispatch to the main thread for the next tick.
        Backbone.dispatchMain {
            val player = ctx.getPlayer() // Get the sender as a player (and require it to be one)
            player.world.spawnEntity(player.location, EntityType.BEE)
        }

        // Halt execution and mark the command as failed.
        ctx.fail("Something is not right!")
    }
}

// In your ManagedLifecycle's onLoad:
override fun onLoad() {
    CommandHandler.register(MyCommand)
}

// In your ManagedLifecycle's onUnload:
override fun onUnload() {
    CommandHandler.unregister(MyCommand)
}
```

### Custom Arguments

You can also create your own custom argument types by extending the `Argument` class. This allows you to define custom parsing and tab-completion logic.

Here is an example of a custom `DoubleArgument`:

```kotlin
class DoubleArgument(name: String, description: String) : Argument<Double>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()
        val completions = if (arg.text.isBlank()) mutableListOf("<$name:double>") else mutableListOf()
        return CompletionResult(completions, arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Double> {
        val arg = current.getNextSingle()
        val value = arg.text.toDoubleOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid double.")
        return ParseResult(value, arg.end)
    }
}
```

You can then use this custom argument in your command definitions:

```kotlin
arguments(
    DoubleArgument("amount", "A decimal number")
)
```

### Custom Formatting and Utilities

Backbone includes a flexible text formatting system that allows you to customize the look and feel of your script's output.

#### Command Feedback Format

You can create a custom `CommandFeedbackFormat` to change how command responses are displayed.

```kotlin
val myFormat = CommandFeedbackFormat("MyPlugin", "#55555")

// You can then pass this format to your command.
object MyCommand : Command("mycommand", "My first command", format = myFormat) {
    // ...
}
```

This will format command responses with a custom prefix and color. The `CommandFeedbackFormat` uses a custom `Alphabet` to encode the handler name, giving it a unique look.

#### Custom Alphabets

You can create your own custom alphabets by implementing the `Alphabet` interface. This allows you to encode text in unique ways, such as the `BoldSmallAlphabet` used by the `CommandFeedbackFormat`.

```kotlin
object MyAlphabet : Alphabet {
    const val ALPHABET = "..." // Your custom alphabet characters

    override fun encode(str: String): String {
        // Your encoding logic here
        return "encoded_string"
    }
}
```

### GUIs

Backbone's GUI framework provides a declarative way to create and manage inventories. The GUI handler automatically manages state on a per-player basis, differentiating each player's unique inventory view.

```kotlin
// Create a Test Inventory Gui with 27 slots
object TestGui : Gui("Test Gui", 27) {
    // 'prepare' is run once during construction.
    // Use this to define the static layout of your GUI, such as placing buttons.
    override fun prepare(inventory: Inventory) {
        inventory.setItem(0, ItemStack(Material.GOLDEN_APPLE))
    }

    // 'open' is run whenever the inventory is first loaded for a player.
    // Use this to dynamically load player-specific data.
    override fun open(player: Player, inventory: Inventory) {
        // GUI has been opened for the player
    }

    // 'close' is called when the inventory is closed.
    // Note: To open another GUI from this event, schedule it for the next tick
    // by wrapping the .open() call in Backbone.dispatchMain {}.
    override fun close(inventory: InventoryCloseEvent) {
        // GUI has been closed
    }

    // 'tick' runs every game tick for open GUIs.
    // Useful for animations and other dynamic logic.
    override fun tick(inventory: Inventory) {
        val randomSlot = (0 until inventory.size).random()
        inventory.setItem(randomSlot, ItemStack(Material.APPLE))
    }

    // 'clicked' runs when a slot is clicked in this GUI.
    override fun clicked(inventory: InventoryClickEvent) {
        // A slot was clicked
    }

    // 'interacted' runs on any interaction (including clicks).
    override fun interacted(inventory: InventoryInteractEvent) {
        // An interaction occurred
    }
}

// In a command or event within your script:
TestGui.open(player)
```
