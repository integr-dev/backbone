package net.integr.backbone.handler

import net.integr.backbone.systems.logger.BackboneLogger

class TestHandler : Handler {
    override val bbl = BackboneLogger("backbone", null)
    override val plugin = null
}