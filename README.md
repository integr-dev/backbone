# Backbone
Backbone is a powerful and flexible framework for Spigot-based Minecraft servers, designed to supercharge plugin development. Its core philosophy is to enable developers to write, test, and update code on a live server without requiring restarts, dramatically accelerating the development lifecycle.

Whether you're a server administrator looking to add custom features with simple scripts or a seasoned developer building complex plugins, Backbone provides the tools you need to be more productive and creative.

## Features

- **Hot-Loadable Scripts:** Write and reload Kotlin scripts without restarting the server, enabling rapid development and iteration.
- **Event System:** A custom event bus that complements Bukkit's event system, offering more control and flexibility.
- **Command Framework:** A simple yet powerful command system with built-in argument parsing and error handling.
- **Storage Abstraction:** Easily manage data with a flexible storage system that supports SQLite databases and typed configuration files.
- **GUI Framework:** A declarative GUI framework for creating complex and interactive inventories.
- **Text Formatting:** A flexible text formatting system with support for custom alphabets and color codes.

## Getting Started

Getting started with Backbone is easy, whether you're a server owner or a developer.

### For Server Owners

1.  **Download:** Grab the latest version of Backbone from the [releases page](https://github.com/integr-dev/backbone/releases).
2.  **Install:** Place the downloaded `.jar` file into your server's `plugins` directory.
3.  **Start:** Start your server. Backbone will generate its necessary folders.
4.  **Scripting:** You can now start writing `.bb.kts` script files in the `scripts/` directory. See the [Hot-Loadable Scripts](#hot-loadable-scripts) section for examples.

### For Developers

If you want to use backbone to develop your server, there is mostly no need to use the backbone jar as a dependency
since the scripting engine exists and has full java classpath support.

If you want to use backbone as a dependency anyway, please use a local lib with the built jar

## Examples

### Hot-Loadable Scripts

Backbone's most powerful feature is its hot-loadable script engine. This allows you to write Kotlin code in script files (`.bb.kts`) and load, unload, and reload them on the fly without needing to restart your server. This is incredibly useful for rapid development, prototyping, and live server updates.

#### Script Location and Aggregation

All script files should be placed in the `scripts/` directory. The script engine will automatically discover and compile any `.bb.kts` files in this location when scripts are loaded.

#### Script Structure

Every script file must evaluate to an object that extends `ManagedLifecycle`. This class provides the necessary hooks for the script engine to manage the script's lifecycle.

A typical script looks like this:

```kotlin
import net.integr.backbone.Backbone
import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.event.BackboneEventHandler
import net.integr.backbone.systems.hotloader.ManagedLifecycle
import net.integr.backbone.systems.hotloader.sustained

// The script file must return an object that extends ManagedLifecycle
object : ManagedLifecycle() {
    // Use 'by sustained()' to create variables that persist across script reloads.
    var counter by sustained(0) 
    
    // Normal variables are reset every time the script is reloaded.
    var otherCounter = 0 

    // Called when the script is loaded or enabled.
    // Use this to register listeners, commands, etc.
    override fun onLoad() {
        Backbone.registerListener(this) 
    }

    // Called when the script is unloaded, disabled, or reloaded.
    // Use this to unregister listeners and clean up resources.
    override fun onUnload() {
        Backbone.unregisterListener(this)
    }

    @BackboneEventHandler
    fun onTick(event: TickEvent) {
        // This event will fire every server tick while the script is enabled.
        counter++
        otherCounter++

        if (counter % 20 == 0) { // Every second
            Backbone.PLUGIN.server.onlinePlayers
                .forEach { it.sendMessage("Sustained Count: $counter | Volatile Count: $otherCounter") }
        }
    }
}
```

#### Managing Scripts

Backbone provides a set of commands to manage your scripts. The base command is `/backbone`, which can be aliased to `/bb`.

-   `/bb scripting`: Lists all discovered scripts and shows whether they are currently enabled or disabled.
-   `/bb scripting reload`: Unloads all current scripts and then loads and enables all scripts from the scripts directory. This is the primary command for applying changes.
-   `/bb scripting enable <script_name>`: Enables a specific script that is currently disabled. This will call the script's `onLoad()` function.
-   `/bb scripting disable <script_name>`: Disables a specific script that is currently enabled. This will call the script's `onUnload()` function.

### Storage and Configuration

Backbone provides a simple and powerful way to manage your plugin's data and configuration through `ResourcePool`s. This system allows you to easily handle databases and configuration files in a structured manner.

#### Resource Pools

A `ResourcePool` is a namespaced container for your plugin's resources. It's recommended to create a separate pool for your plugin to avoid conflicts.

```kotlin
// Create a resource pool for your plugin's storage
val myPluginStorage = ResourcePool.fromStorage("myplugin")

// Create a resource pool for your plugin's configuration
val myPluginConfig = ResourcePool.fromConfig("myplugin")
```

This will create directories at `storage/myplugin/` and `config/myplugin/` respectively.

#### Configuration

You can easily manage typed configuration files. Backbone handles the serialization and deserialization of your data classes.

First, define a data class for your configuration:

```kotlin
@Serializable // Requires the kotlinx.serialization plugin
data class MyConfig(val settingA: String = "default", val settingB: Int = 10)
```

Then, use the `config()` function on your resource pool to get a handler for it:

```kotlin
// Get a handler for a config file named 'settings.json'
val configHandler = myPluginConfig.config<MyConfig>("settings.json")

// Get the current config, or the default if it doesn't exist
val currentConfig = configHandler.get()
println("Setting A: ${currentConfig.settingA}")

// Modify and save the config
configHandler.set(currentConfig.copy(settingB = 20))
```

#### Databases

Backbone provides a simple and efficient way to work with SQLite databases. The `DatabaseConnection` class manages the connection lifecycle, and the `StatementCreator` provides safe and convenient methods for executing queries.

```kotlin
// Get a connection to a database file named 'playerdata.db'
val dbConnection = myPluginStorage.database("playerdata.db")

// The useConnection block handles connection setup and teardown automatically.
dbConnection.useConnection { 
    // The 'this' context is a StatementCreator instance.

    // Execute a simple statement to create a table if it doesn't exist.
    execute("""
        CREATE TABLE IF NOT EXISTS players (
            uuid TEXT PRIMARY KEY,
            name TEXT NOT NULL
        );
    """)

    // Use a prepared statement to safely insert data.
    preparedStatement("INSERT INTO players (uuid, name) VALUES (?, ?)") {
        setString(1, "some-uuid")
        setString(2, "PlayerName")
        executeUpdate()
    }

    // Use a query to retrieve data and map it to a custom object.
    val playerName = query("SELECT name FROM players WHERE uuid = 'some-uuid'") { resultSet ->
        resultSet.getString("name")
    }

    println("Found player: $playerName")
}
```

The `useConnection` block ensures that the database connection is properly acquired and released, preventing resource leaks. It also provides a `StatementCreator` context, giving you direct access to the `execute`, `query`, and `preparedStatement` helper methods.

### Custom Events

Backbone's event system allows you to create and listen for custom events, giving you more control over your plugin's behavior.


```kotlin
// Define a custom event
class MyCustomEvent(val message: String) : Event()

// Register a listener for the custom event
// Priority is from -3 up to 3 with 0 being normal, lower means first
@BackboneEventHandler(EventPriority.THREE_BEFORE)
fun onMyCustomEvent(event: MyCustomEvent) {
    println("Received custom event: ${event.message}")
}

// Fire the custom event
EventBus.post(MyCustomEvent("Hello, world!"))
```

### Commands

Backbone's command framework makes it easy to create and manage commands.
You can use arguments, subcommands, permission checks and by default commands are async
This is why you need to wrap calls that affect the server in `Backbone.dispatchMain {}`


```kotlin
// Define a command
object MyCommand : Command("mycommand", "My first command") {
    val perm = PermissionNode("myplugin")

    override fun onBuild() {
        // Provide another command as a sub
        // The command will be executed with /<parent> <child1> <child2> <...> <argument1> <argument2> <...>
        subCommands(
            MySubCommand
        )

        // Create arguments for the command
        arguments(
            scriptArgument("text", "My string")
        )
    }
    
    override suspend fun exec(ctx: Execution) {
        ctx.requirePermission(perm.derive("mycommand")) // Create a permission check for "myplugin.mycommand"
        
        val text = ctx.get<String>("text") // Get a filled argument

        ctx.respond("Hello ${ctx.sender.name}: $text") // Respond send a formatted message

        // Calls that affect the server are to wrap in "dispatchMain"
        // This runs them on the next tick, on the server thread
        Backbone.dispatchMain {
            val player = ctx.getPlayer() // Get the sender as player (and require it to be one)
            player.world.spawnEntity(player.location, EntityType.BEE)
        }
        
        // Mark the execution as failed, throws an exception and halts the execution here
        ctx.fail("Something is not right!")
    }
}

// Register the command
CommandHandler.register(MyCommand)
```

### Custom Arguments

You can also create your own custom argument types by extending the `Argument` class. This allows you to define custom parsing and completion logic.

Here is an example of a custom `DoubleArgument`:

```kotlin
class DoubleArgument(name: String, description: String) : Argument<Double>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:double>") else mutableListOf(), arg.end)
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

Backbone includes a flexible text formatting system that allows you to customize the look and feel of your plugin's output.

#### Command Feedback Format

You can create a custom `CommandFeedbackFormat` to change how command responses are displayed. This is useful for creating a consistent brand for your server or plugin.

```kotlin
val myFormat = CommandFeedbackFormat("MyPlugin", "#55555")

// You can then pass this format to your command
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
    }
}
```

### GUIs

Backbone's GUI framework provides a declarative way to create and manage inventories.
The inventory handler can automatically differentiate between players inventories
and will manage their states for you.

```kotlin
// Create a Test Inventory Gui with 27 slots
object TestGui : Gui("Test Gui" , 27) {
    // Prepare is ran once during construction to set up base layouts
    // Use this to for example load fixed content and for example buttons
    override fun prepare(inventory: Inventory) {
        inventory.setItem(0, ItemStack(Material.GOLDEN_APPLE))
    }

    // Ran whenever the inventory is first loaded for a player
    // Use this to dynamically load data
    override fun open(player: Player, inventory: Inventory) {
        // Opened!
    }

    // Called when the inventory is closed
    // Do not use this to re-open inventories!
    // If you want to re-open another inventory wrap your
    // .open(<player>) call with Backbone.dispatchMain {}
    // To schedule the operation for the next tick
    override fun close(inventory: InventoryCloseEvent) {
        // Closed!
    }

    // Runs every game tick
    // Used for animations and other logic
    override fun tick(inventory: Inventory) {
        val randomSlot = (0 until inventory.size).random()
        inventory.setItem(randomSlot, ItemStack(Material.APPLE))
    }

    // Runs when a slot is clicked in the inventory
    override fun clicked(inventory: InventoryClickEvent) {
        // Clicked!
    }

    // Runs on any interaction (including clicks)
    override fun interacted(inventory: InventoryInteractEvent) {
        // Interacted!
    }
}

// Open the GUI for a player
TestGui().open(player)
```
