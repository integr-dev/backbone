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

/**
 * A custom alphabet that encodes strings into a tiny letter style.
 * **Important:** This alphabet will not handle 'Q' and 'q' correctly since there is no char for it.
 * @since 1.3.0
 */
object TinyAlphabet : Alphabet {
    override val alphabet = "ᴬᴮᶜᴰᴱᶠᴳᴴᴵᴶᴷᴸᴹᴺᴼᴾQᴿˢᵀᵁⱽᵂˣʸᶻᵃᵇᶜᵈᵉᶠᵍʰⁱʲᵏˡᵐⁿᵒᵖQʳˢᵗᵘᵛʷˣʸᶻ"
}