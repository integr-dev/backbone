package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.ArgChain
import net.integr.backbone.systems.command.arguments.Argument

class IntegerArgument(name: String, description: String) : Argument<Int>(name, description) {
    // A simple integer argument parser.

    override fun getCompletions(argChain: ArgChain): CompletionResult {
        val current = argChain.current() ?: return CompletionResult.EMPTY

        return CompletionResult(listOf("<$name:int>"), needsMoreInput = current.isBlank())
    }

    override fun parse(argChain: ArgChain): Int {
        val current = argChain.current() ?: throw IllegalArgumentException("Argument '$name' is required.")

        return current.toIntOrNull() ?: throw IllegalArgumentException("Argument '$name' must be a valid integer.")
    }
}