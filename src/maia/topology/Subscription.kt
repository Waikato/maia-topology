package maia.topology

import kotlinx.coroutines.channels.Channel
import maia.topology.io.Input
import maia.topology.io.Output
import maia.util.debugln

/**
 * Represents a subscription from the output of a publishing node to
 * the input of a subscribing node.
 *
 * @param from  The output from which data will be sourced.
 * @param to    The input to which the data will be provided.
 */
data class Subscription(val from : Output<*>, val to : Input<*>) {

    // Register the subscription with the participatory nodes
    init {
        to.subscription = this
        from.subscriptions.add(this)
    }

    // region Channel

    /** The channel via which data is delivered from the output to the input. */
    internal lateinit var channel : Channel<Any?>

    /** Whether this subscription is closed from the connected input's point-of-view. */
    val isClosedToInput
        get() = channel.isClosedForReceive

    /** Whether this subscription is closed from the connected output's point-of-view. */
    val isClosedToOutput
        get() = channel.isClosedForSend

    fun close() {
        if (isClosedToInput) return
        debugln("Closing subscription between $from and $to")
        channel.close()
    }

    /**
     * Suspends the caller until an item is sent to the subscription,
     * and returns the sent item.
     */
    suspend fun receive() = channel.receive()

    /**
     * Sends an item over this subscription, and suspends the caller
     * until the item is received.
     *
     * @param element   The item to send.
     */
    suspend fun send(element : Any?) = channel.send(element)

    /**
     * Resets the subscription before execution of the topology, so that if this
     * subscription closed in prior executions, it will be re-opened for new runs.
     */
    internal fun reset() {
        channel = Channel()
    }

    // endregion

}
