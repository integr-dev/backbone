package net.integr.backbone.systems.async

import net.integr.backbone.Backbone
import java.util.concurrent.CompletableFuture

object AsyncHandler {
    fun <T> runAsync(task: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        Backbone.SCHEDULER.runTaskAsynchronously(Backbone.PLUGIN!!, Runnable {
            try {
                val result = task()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        })

        future.exceptionally {
            throw it // Rethrow exception to be handled by the caller
        }

        return future
    }
}