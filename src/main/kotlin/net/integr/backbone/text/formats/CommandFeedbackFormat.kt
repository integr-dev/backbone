package net.integr.backbone.text.formats

import net.integr.backbone.systems.text.TextColor
import net.integr.backbone.text.alphabets.BoldSmallAlphabet
import net.integr.backbone.systems.text.TextFormat

class CommandFeedbackFormat(handler: String, val handlerColor: String) : TextFormat {
    private val handlerName = BoldSmallAlphabet.encode(handler)

    override fun format(str: String): String {
        return TextColor.parse( "&$handlerColor$handlerName &#2b2b2a» &#a9ada8$str")
    }

    fun formatNoPrefix(str: String): String {
        return TextColor.parse( "&#a9ada8$str")
    }

    fun formatErr(str: String): String {
        return TextColor.parse( "&$handlerColor$handlerName &#2b2b2a» &#f5424b$str")
    }
}