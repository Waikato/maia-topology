package maia.topology.node.base

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import maia.topology.NodeConfiguration
import maia.topology.ExecutionState

/**
 * Base class for source nodes which inject items into a running topology
 * from an externally-controlled process.
 *
 * @param block             The configuration block for the node's configuration.
 * @param maxConnections    The maximum number of connections allowed to the primary output.
 * @param C                 The type of configuration this node takes.
 * @param T                 The type of item this source produces.
 */
open class InjectionSource<C : NodeConfiguration, T>(
        block : C.() -> Unit = {},
        maxConnections : Int = -1
) : Source<C, T>(block, maxConnections) {

    /** The channel by which elements are injected into the topology. */
    private var ingressChannel by ExecutionState { Channel<T>() }

    /**
     * Injects an item into the topology. Non-blocking.
     *
     * @param item  The item to inject.
     */
    fun inject(item : T) {
        scope.launch { injectSuspend(item) }
    }

    /**
     * Injects an item into the topology, suspending the
     * calling coroutine until it has been received.
     */
    suspend fun injectSuspend(item : T) {
        ingressChannel.send(item)
    }

    /**
     * Allows this injection source to be finished externally.
     * Call this if you have no more items to inject into the topology.
     */
    fun close() {
        ingressChannel.close()
    }

    final override suspend fun produce() : T {
        try {
            return ingressChannel.receive()
        } catch (e : ClosedReceiveChannelException) {
            abort()
        }
    }

}
