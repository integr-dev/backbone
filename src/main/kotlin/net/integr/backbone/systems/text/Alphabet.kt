package net.integr.backbone.systems.text

interface Alphabet {
    fun encode(str: String): String

    companion object {
        const val DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    }
}