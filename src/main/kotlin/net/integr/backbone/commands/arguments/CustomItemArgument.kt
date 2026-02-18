package net.integr.backbone.commands.arguments

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.argument.Argument
import net.integr.backbone.systems.hotloader.ScriptStore
import net.integr.backbone.systems.item.ItemHandler

fun customItemArgument(name: String, description: String): Argument<String> {
    return CustomItemArgument(name, description)
}

class CustomItemArgument(name: String, description: String) : Argument<String>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val items = ItemHandler.items.keys
        val isQuoted = current.value.startsWith("\"")

        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val hasClosingQuote = isQuoted && arg.found

        val itemMap = items.map { it + "\"" }.toMutableList()

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(itemMap, arg.end)
        } else {
            CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:item>", *items.toTypedArray()) else items.toMutableList(), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<String> {
        val items = ItemHandler.items.keys
        val isQuoted = current.value.startsWith("\"")
        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val text = if (isQuoted) {
            if (!arg.found) throw CommandArgumentException("Argument '$name' is missing a closing quotation mark.")
            arg.text.substring(1, arg.text.length - 1)
        } else {
            arg.text
        }

        if (!items.contains(text)) throw CommandArgumentException("Argument '$name' is not a valid item.")

        return ParseResult(text, arg.end)
    }
}