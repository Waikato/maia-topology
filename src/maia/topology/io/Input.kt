package maia.topology.io

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import maia.topology.Node
import maia.topology.Subscription
import maia.topology.io.error.InputAlreadySubscribedException
import maia.topology.io.error.InputClosedDuringPullException
import maia.util.debugln
import kotlin.reflect.KProperty

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
open class Input<T> : Throughput<Input<T>, T>() {

    val isClosed : Boolean
        get() = subscription?.isClosedToInput ?: true

    internal var subscription : Subscription? = null
        set(value) {
            if (field != null) throw InputAlreadySubscribedException(this)
            field = value
        }

    override val isSubscribed : Boolean
        get() = subscription != null

    suspend fun pull() : T {
        try {
            return subscription!!.receive() as T
        } catch (e : ClosedReceiveChannelException) {
            throw InputClosedDuringPullException(this, e)
        } finally {
            debugln("$this pulling...")
        }
    }

    /**
     * Attempts to pull an item from this input, returning the
     * item if successful, or null if the input closes.
     *
     * @return  An item of the input's type, or null if the input closes.
     */
    suspend fun pullOrNull() : T? {
        return try {
            pull()
        } catch (e : InputClosedDuringPullException) {
            null
        }
    }

    suspend fun close() {
        subscription?.close()
    }

    internal suspend fun closeAndDrain() {
        close()
        debugln("Draining $this...")
        val sub = subscription
        if (sub == null)
            debugln("$this is not subscribed")
        else {
            while (!isClosed) {
                debugln("Attempting drain of $this...")
                val item = pull()
                debugln("Drained 1 item ($item) from $this")
            }
            debugln("Finished draining $this")
        }
    }

    override fun onDelegation(
        owner: Node<*>,
        property: KProperty<*>,
        name: String
    ) {
        owner.registerInput(this, name)
    }

    override fun getValue() : Input<T> {
        return this
    }
}

