package maia.topology

import maia.topology.error.NotExecutingError
import maia.util.Absent
import maia.util.Optional
import maia.util.Present
import maia.util.property.SingleUseReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Instances of this class act as delegates for node-state which should be
 * reset on each subsequent execution of the node.
 *
 * @param initialiser   A function to call to initialise the state.
 * @param T             The type of the value of the state.
 */
class ExecutionState<T>(
        private val initialiser : () -> T
) : SingleUseReadWriteProperty<Node<*>, T>() {

    /** The actual value of the state. */
    private var state : Optional<T> = Absent

    override fun getValue(): T = ensureExecuting {
        // Initialise the state if it hasn't been explicitly set
        if (state is Absent)
            state = Present(initialiser())

        return state.get()
    }

    override fun setValue(value: T) = ensureExecuting {
        state = Present(value)
    }

    override fun onDelegation(
        owner : Node<*>,
        property : KProperty<*>,
        name : String
    ) {
        // Register the state with the node
        owner.registerExecutionState(this)
    }

    /**
     * Resets the state to its initial value.
     */
    internal fun reset() {
        state = Absent
    }

    /**
     * Ensures the node that owns this state is executing.
     *
     * @param block     The action to perform under the assumption of execution.
     * @return          The result of the action.
     * @param R         The result type.
     */
    private inline fun <R> ensureExecuting(block : () -> R) : R {
        if (owner.scopeInternal == null)
            throw NotExecutingError(
                    "Attempted to access execution state '$name' " +
                            "of ${owner.fullName} " +
                            "outside of an execution context"
            )

        return block()
    }
}
