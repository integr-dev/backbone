package net.integr.backbone.systems.command.arguments

abstract class Argument<T : Any>(val name: String, val description: String) {
    abstract fun getCompletions(current: ArgumentInput): CompletionResult

    abstract fun parse(current: ArgumentInput): ParseResult<T>

    class CompletionResult(val completions: List<String>, val end: Int) {
        companion object {
            val EMPTY = CompletionResult(emptyList(), -1)
        }
    }

    class ParseResult<T : Any>(val value: T, val end: Int)

    class ArgumentInput(val value: String) {
        fun getNextSingle(): SubInputWithResult {
            val str = value.substringBefore(' ')
            return SubInputWithResult(str, str.length, true)
        }

        fun getNextToToken(token: Char): SubInputWithResult {
            val str = value.substringBefore(token)
            return SubInputWithResult(str, str.length, str.length == value.length)
        }

        fun getNextContaining(token: Char): SubInputWithResult {
            val str = value.substring(1).substringBefore(token)
            val hasResult = str.length + 2 <= value.length
            val actString = token + str + if (hasResult) token else ""
            return SubInputWithResult(actString, actString.length, hasResult)
        }

        data class SubInputWithResult(val text: String, val end: Int, val found: Boolean) {
            fun getLastSplitBySpace(): String {
                return text.substringAfterLast(' ')
            }
        }
    }
}