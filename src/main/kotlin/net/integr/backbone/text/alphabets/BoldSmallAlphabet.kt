/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.backbone.text.alphabets

import net.integr.backbone.systems.text.Alphabet
import kotlin.text.iterator

object BoldSmallAlphabet : Alphabet {
    const val ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘQʀꜱᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘQʀꜱᴛᴜᴠᴡxʏᴢ"

    override fun encode(str: String): String {
        val sb = StringBuilder()

        for (char in str) {
            val index = Alphabet.Companion.DEFAULT_ALPHABET.indexOf(char)
            if (index != -1) {
                sb.append(ALPHABET[index])
            } else {
                sb.append(char)
            }
        }

        return sb.toString()
    }
}