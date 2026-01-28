package net.integr.backbone.handler

import net.integr.backbone.systems.logger.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin

interface Handler {
    val bbl: BackboneLogger
    val plugin: JavaPlugin?
}