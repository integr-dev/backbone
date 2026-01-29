package net.integr.backbone.systems.permission

class PermissionNode(val id: String) {
    fun derive(id: String): PermissionNode {
        return PermissionNode("${this.id}.$id")
    }
}