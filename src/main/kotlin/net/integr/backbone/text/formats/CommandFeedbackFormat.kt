package net.integr.backbone.text.formats

import net.integr.backbone.systems.text.append
import net.integr.backbone.systems.text.component
import net.integr.backbone.text.alphabets.BoldSmallAlphabet
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import java.awt.Color

class CommandFeedbackFormat(handler: String, val handlerColor: Color) {
    private val handlerName = BoldSmallAlphabet.encode(handler)

    fun format(str: String): BaseComponent {
        return component {
            append(handlerName) {
                color(ChatColor.of(handlerColor))
            }

            append(" » ") {
                color(ChatColor.of(Color(43, 43, 42)))
            }

            append(str) {
                color(ChatColor.of(Color(169, 173, 168)))
            }
        }
    }

    fun formatErr(str: String): BaseComponent {
        return component {
            append(handlerName) {
                color(ChatColor.of(handlerColor))
            }

            append(" » ") {
                color(ChatColor.of(Color(43, 43, 42)))
            }

            append(str) {
                color(ChatColor.of(Color(245, 66, 75)))
            }
        }
    }
}