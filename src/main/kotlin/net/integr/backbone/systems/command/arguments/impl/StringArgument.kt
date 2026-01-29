package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun stringArgument(name: String, description: String): Argument<String> {
    return StringArgument(name, description)
}

class StringArgument(name: String, description: String) : Argument<String>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val isQuoted = current.value.startsWith("\"")

        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()
        val last = if (isQuoted) arg.getLastSplitBySpace() + "\"" else arg.text

        val hasClosingQuote = isQuoted && arg.found

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(mutableListOf(last), arg.end)
        } else {
            CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:string>") else mutableListOf(), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<String> {
        val isQuoted = current.value.startsWith("\"")
        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val text = if (isQuoted) {
            if (!arg.found) throw CommandArgumentException("Argument '$name' is missing a closing quotation mark.")
            arg.text.substring(1, arg.text.length - 1)
        } else {
            arg.text
        }

        if (text.isBlank()) throw CommandArgumentException("Argument '$name' cannot be blank.")

        return ParseResult(text, arg.end)
    }
}