package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.ArgChain
import net.integr.backbone.systems.command.arguments.Argument

class StringArgument(name: String, description: String) : Argument<String>(name, description) {
    // A simple string argument that accepts any input string.
    // Also supports quoted strings with spaces for multi-word inputs.

    override fun getCompletions(argChain: ArgChain): CompletionResult {
        val current = argChain.current() ?: return CompletionResult.EMPTY
        val isQuoted = current.startsWith("\"")
        val last = argChain.last() ?: ""
        val hasClosingQuote = if (isQuoted) hasClosingQuote(argChain) else false

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(listOf(last + "\""), needsMoreInput = true)
        } else {
            CompletionResult(listOf("<$name:string>"), needsMoreInput = current.isBlank() && !isQuoted)
        }
    }

    fun hasClosingQuote(argChain: ArgChain): Boolean {
        var isFirst = true
        while (!argChain.isEmpty()) {
            val current = argChain.current() ?: break
            if (current.endsWith("\"") && (isFirst && current.length > 1 || !isFirst)) {
                return true
            }

            argChain.moveNext()
            isFirst = false
        }

        return false
    }

    override fun parse(argChain: ArgChain): String {
        val start = argChain.current() ?: throw IllegalArgumentException("Argument '$name' is required.")

        val isQuoted = start.startsWith("\"")

        if (isQuoted) {
            // Build a string until we find a closing quote
            val stringBuilder = StringBuilder()
            while (!argChain.isEmpty()) {
                val current = argChain.current() ?: break

                stringBuilder.append(current)
                if (current.endsWith("\"") && stringBuilder.length > 1) {
                    val result = stringBuilder.toString()
                    return result.substring(1, result.length - 1)
                } else {
                    stringBuilder.append(" ")
                }

                argChain.moveNext()
            }

            throw IllegalArgumentException("No closing quote for argument '$name'.")
        } else {
            return start
        }
    }
}