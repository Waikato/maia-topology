package maia.topology.io

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.coroutineScope
import maia.topology.Node
import maia.topology.Subscription
import maia.topology.io.error.FullyConnectedError
import maia.util.debugln
import kotlin.reflect.KProperty

/**
 * An output connector on a node.
 *
 * @param maxConnections    The maximum number of inputs that can connect to this
 *                          output (if less than zero then no restriction is made).
 * @param T                 The type of values that this output emits.
 */
open class Output<T>(maxConnections : Int = -1) : Throughput<Output<T>, T>() {

    /** The list of subscriptions to this output by inputs on other nodes. */
    internal val subscriptions : SubscriptionList = SubscriptionList(maxConnections)

    /** Whether this output is closed (no more items can be pushed). */
    val isClosed : Boolean
        get() {
            return subscriptions.size == 0 || subscriptions.all { it.isClosedToOutput }
        }

    override val isSubscribed: Boolean
        get() = subscriptions.size > 0

    /**
     * Pushes an item to this output, to be delivered to all
     * connected inputs.
     *
     * @param item
     *          The item to push to the output.
     * @return
     *          Whether the item was delivered.
     */
    suspend fun push(item : T) : Boolean {
        debugln("$this pushing...")

        // If nothing is subscribed to us, we can't push...
        if (subscriptions.size == 0) return false

        return coroutineScope {
            val jobs = subscriptions
                    .map {
                        async {
                            if (it.isClosedToOutput)
                                false
                            else try {
                                    it.send(item)
                                    true
                            } catch (e : ClosedSendChannelException) {
                                // Output closed during push, ignore as item is
                                // pushed into the aether and output will skip
                                // any further push call
                                false
                            }
                        }
                    }
            val results = jobs.map {
                it.await()
            }
            val result = results.reduceRight { l, r -> l || r }
            result
        }
    }

    fun close() {
        subscriptions.forEach { it.close() }
    }

    override fun onDelegation(
        owner : Node<*>,
        property : KProperty<*>,
        name : String
    ) {
        owner.registerOutput(this, name)
    }

    override fun getValue() : Output<T> {
        return this
    }

    /**
     * A list of subscriptions to an output, ensuring that the maximum size
     * of the list is never more than some constant.
     *
     * @param maxSize   The maximum number of subscriptions allowed in the list.
     */
    internal class SubscriptionList(val maxSize : Int) : ArrayList<Subscription>() {
        override fun add(element: Subscription): Boolean {
            // Ensure the size of the list never exceeds the specified limit
            if (maxSize >= 0 && size == maxSize) throw FullyConnectedError(maxSize)

            return super.add(element)
        }
    }

}
