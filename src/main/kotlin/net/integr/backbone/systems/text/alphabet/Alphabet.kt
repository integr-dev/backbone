package net.integr.backbone.systems.text.alphabet

interface Alphabet {
    fun encode(str: String): String

    companion object {
        const val DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    }
}