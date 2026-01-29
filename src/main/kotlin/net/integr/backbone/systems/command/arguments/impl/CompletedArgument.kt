package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.Argument

fun <T : Any> completedArgument(argument: Argument<T>, options: List<String>): Argument<T> {
    return CompletedArgument(argument, options)
}

class CompletedArgument<T : Any>(val argument: Argument<T>, val options: List<String>) : Argument<T>(argument.name, argument.description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val res = argument.getCompletions(current)
        res.completions += options
        return res
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        return argument.parse(current)
    }
}