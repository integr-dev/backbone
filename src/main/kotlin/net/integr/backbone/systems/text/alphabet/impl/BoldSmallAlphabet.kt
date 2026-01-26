package net.integr.backbone.systems.text.alphabet.impl

import net.integr.backbone.systems.text.alphabet.Alphabet

object BoldSmallAlphabet : Alphabet {
    const val ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘQʀꜱᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘQʀꜱᴛᴜᴠᴡxʏᴢ"

    override fun encode(str: String): String {
        val sb = StringBuilder()

        for (char in str) {
            val index = Alphabet.DEFAULT_ALPHABET.indexOf(char)
            if (index != -1) {
                sb.append(ALPHABET[index])
            } else {
                sb.append(char)
            }
        }

        return sb.toString()
    }
}