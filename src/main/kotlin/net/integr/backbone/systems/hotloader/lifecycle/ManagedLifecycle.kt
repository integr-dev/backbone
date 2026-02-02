package net.integr.backbone.systems.hotloader.lifecycle

import org.bukkit.event.Listener

abstract class ManagedLifecycle : Listener {
    private var sustainedStates: Set<LifecycleSustainedState<*>> = mutableSetOf()

    abstract fun onLoad()
    abstract fun onUnload()

    fun trackState(state: LifecycleSustainedState<*>) {
        sustainedStates += state
    }

    fun updateStatesFrom(lifecycle: ManagedLifecycle) {
        for (state in lifecycle.sustainedStates) {
            val newState = sustainedStates.firstOrNull() { it.id == state.id }
            newState?.dangerouslySetState(state.dangerouslyGetState())
        }
    }
}