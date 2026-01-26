package net.integr.backbone.systems.text

object TextColor {
    fun parse(str: String): String {
        var out = str
        // str like Hello &#ff5555 World
        val regex = "&#[a-fA-F0-9]{6}".toRegex()
        val matches = regex.findAll(str)
        for (match in matches) {
            val colorCode = match.value
            val color = parseSingular(colorCode)
            out = out.replace(colorCode, color)
        }

        return out
    }

    fun parseSingular(color: String): String {
        return net.md_5.bungee.api.ChatColor.of(color.removePrefix("&")).toString()
    }
}