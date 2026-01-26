package net.integr.backbone.systems.command.arguments

abstract class Argument<T : Any>(val name: String, val description: String) {
    abstract fun getCompletions(argChain: ArgChain): CompletionResult

    abstract fun parse(argChain: ArgChain): T

    class CompletionResult(val completions: List<String>, val needsMoreInput: Boolean = false) {
        companion object {
            val EMPTY = CompletionResult(emptyList(), false)
        }
    }
}