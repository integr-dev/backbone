package net.integr.backbone.systems.hotloader

import net.integr.backbone.Backbone
import org.bukkit.event.Listener
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm


@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassloader(classLoader = Backbone::class.java.classLoader, wholeClasspath = true)
    }
})

@KotlinScript(
    fileExtension = "bb.kts",
    compilationConfiguration = ScriptConfiguration::class
)
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