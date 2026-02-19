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

package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

abstract class EntityBackedObject<T : Entity> {
    var entity: T? = null
        protected set

    abstract fun spawn(world: World, location: Location)

    open fun despawn() {
        entity?.remove()
        entity = null
    }

    open fun exists(): Boolean {
        return !(entity?.isDead ?: true)
    }
}