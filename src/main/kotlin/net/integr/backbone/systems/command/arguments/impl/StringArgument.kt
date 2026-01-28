package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.arguments.Argument

fun Command.stringArgument(name: String, description: String) {
    argument(StringArgument(name, description))
}

class StringArgument(name: String, description: String) : Argument<String>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val isQuoted = current.value.startsWith("\"")

        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()
        val last = if (isQuoted) arg.getLastSplitBySpace() + "\"" else arg.text

        val hasClosingQuote = isQuoted && arg.found

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(listOf(last), arg.end)
        } else {
            CompletionResult(if (arg.text.isBlank()) listOf("<$name:string>") else emptyList(), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<String> {
        val isQuoted = current.value.startsWith("\"")
        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val text = if (isQuoted) {
            if (!arg.found) throw IllegalArgumentException("Argument '$name' is missing a closing quotation mark.")
            arg.text.substring(1, arg.text.length - 1)
        } else {
            arg.text
        }

        if (text.isBlank()) throw IllegalArgumentException("Argument '$name' cannot be blank.")

        return ParseResult(text, arg.end)
    }
}