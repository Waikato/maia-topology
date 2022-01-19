package maia.topology

import kotlinx.coroutines.*
import maia.configure.Configurable
import maia.configure.visitation.visit
import maia.topology.error.NodeMissingMetadataError
import maia.topology.error.NotExecutingError
import maia.topology.io.*
import maia.topology.io.error.InputClosedDuringPullException
import maia.topology.io.error.InputException
import maia.topology.io.error.InputsAllClosedDuringSelectException
import maia.topology.io.error.RequiredInputException
import maia.topology.io.util.joinNames
import maia.topology.io.util.select
import maia.topology.util.LinearNodeConfigurationStringWriter
import maia.util.*
import maia.util.property.CachedReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * A node in a topology graph.
 *
 * @param block     A configuration block for the node's configuration.
 * @param C         The type of configuration the node takes.
 */
abstract class Node<C : NodeConfiguration>(block : C.() -> Unit = {}) : Configurable<C>(block) {

    /** The name of the node (from its configuration). */
    val name : String
        get() = configuration.name

    /** The full-name of the node (includes its type). */
    val fullName : String
        get() = "${this::class.simpleName}-node '$name'"

    /** Internal access to the scope this node is executing in. */
    internal var scopeInternal : CoroutineScope? = null

    /** Access to the execution scope for node sub-types. */
    protected val scope : CoroutineScope
        get() = scopeInternal ?: throw NotExecutingError(
                "Attempted to access the execution scope of " +
                        "$fullName outside of an execution context"
        )

    // region Execution

    /**
     * Ensures that the node's scope is dataset/cleared no matter how the
     * block terminates.
     *
     * @param scope     The scope to save for the duration of the block.
     * @param block     The block to execute.
     * @return          The return value of the block.
     * @param R         The return type of the block.
     */
    private inline fun <R> withScope(scope : CoroutineScope, block : () -> R) : R {
        // Save our co-routine scope
        this@Node.scopeInternal = scope

        // Run the block in a try/finally block so the scope
        // is cleared if we exit by exception
        try {
            return block()
        } finally {
            this@Node.scopeInternal = null
        }
    }

    /**
     * Executes the node in the given co-routine scope.
     *
     * @param scope     The scope to launch the node in.
     * @return          The job reference to the node's co-routine.
     */
    internal suspend fun execute(scope: CoroutineScope) : Job = scope.launch {
        withScope(this) {
            // Print the running signature of the node
            debugln("$fullName started\n" +
                    "  Inputs: ${inputs.values.joinNames(true)}\n" +
                    "  Outputs: ${outputs.values.joinNames(true)}\n" +
                    "  Config: ${LinearNodeConfigurationStringWriter().visit(configuration)}\n" +
                    "  Description: ${metadata.description}")

            // Reset all dynamic state
            resetDynamicState()

            // Execute the lifecycle of the node
            try {
                main()
            } catch (e : AbortNode) {
                // Do nothing
            } catch (e : CancellationException) {
                onCancel()
                throw e
            }

            // Close all inputs and outputs so that connected nodes can
            // determine if they should also stop
            for (input in inputs.values) input.closeAndDrain()
            for (output in outputs.values) output.close()

            debugln("$fullName completed")
        }
    }

    /**
     * The main execution body of the node.
     */
    abstract suspend fun main()

    /**
     * Called when the node is cancelled while executing. There is no
     * guarantee that any of [onStart], [main], or [onStop] have been
     * called by this point, so care must be taken with the initialisation
     * state of the node's members.
     */
    protected open suspend fun onCancel() {}

    /**
     * Singleton exception for indicating that the main-body of the node
     * should abort immediately.
     */
    private object AbortNode : Exception()

    /**
     * Aborts the node's main method immediately.
     */
    protected fun abort() : Nothing {
        throw AbortNode
    }

    /**
     * Launches an asynchronous task in the node's scope.
     */
    protected fun doAsync(block : suspend CoroutineScope.() -> Unit) = scope.launch(block = block)

    /**
     * Attempts to pull an item from the input, aborting the node if it fails.
     *
     * @receiver    The input to pull from.
     * @return      The value pulled from the input.
     */
    protected suspend fun <I> Input<I>.pullOrAbort() : I {
        return try {
            pull()
        } catch (e : InputClosedDuringPullException) {
            abort()
        }
    }

    /**
     * Attempts to select from a group of inputs, aborting the node if
     * all inputs fail.
     *
     * @receiver        The collection of inputs to select from.
     * @param required  The set of required inputs, which will cause the select
     *                  to fail if they are missing or closed.
     * @param block     The code to execute for the selected input.
     * @return          The result of the [block].
     * @param I         The type of the inputs.
     * @param R         The return-type of the [block].
     */
    protected suspend inline fun <I, R> Collection<Input<out I>>.selectOrAbort(
            required : Set<Input<out I>>? = null,
            block : (Input<out I>, I) -> R
    ) : R {
        // Select from the inputs, and abort if it fails
        val selectPair : Pair<Input<out I>, I>
        try {
            selectPair = select()
        } catch (e : InputException) {
            when (e) {
                is InputsAllClosedDuringSelectException,
                is RequiredInputException -> abort()
                else -> throw e
            }
        }

        // Execute the block on the selected input/value
        return block(selectPair.first, selectPair.second)
    }

    // endregion

    // region Input/Output/DynamicState Delegates

    /** All inputs attached to this node. */
    internal val inputs = HashMap<String, Input<*>>()

    /**
     * Registers an input with the node when it is delegated.
     *
     * @param input     The input being attached to the node.
     */
    internal fun registerInput(input : Input<*>, name: String) {
        inputs[name] = input
    }

    /** All outputs attached to this node. */
    internal val outputs = HashMap<String, Output<*>>()

    /**
     * Registers an output with the node when it is delegated.
     *
     * @param output    The output being attached to the node.
     */
    internal fun registerOutput(output : Output<*>, name: String) {
        outputs[name] = output
    }

    /** All execution state registered with the node. */
    internal val executionState = HashSet<ExecutionState<*>>()

    /**
     * Registers execution state with the node when it is delegated.
     *
     * @param state     The state being attached to the node.
     */
    internal fun registerExecutionState(state : ExecutionState<*>) {
        executionState.add(state)
    }

    internal fun iterateThroughputs() : Iterator<Throughput<*, *>> {
        return chain(inputs.values.iterator(), outputs.values.iterator())
    }

    /**
     * Resets the dynamic state of the node.
     */
    private fun resetDynamicState() {
        executionState.forEach { it.reset() }
    }

    // endregion

    // region Metadata

    /** The metadata for this type of node. */
    val metadata : Metadata
        get() = this::class.metadata

    /**
     * Class metadata object for nodes.
     *
     * @param description   A description of the functionality of this type of node.
     */
    data class Metadata(
            val description : String
    )

    /**
     * Annotation which defines the metadata for a type of node.
     *
     * @param description   A description of the functionality of this type of node.
     */
    @Target(AnnotationTarget.CLASS)
    annotation class WithMetadata(
            val description : String
    )

    // endregion

    // region Other

    /**
     * Gets the set of all subscriptions this node is a part of.
     *
     * @return  The set of subscriptions.
     */
    internal fun allSubscriptions() : Set<Subscription> = buildSet {
            inputs.values.forEach {
                it.subscription?.apply { add(this) }
            }
            outputs.values.forEach { output ->
                output.subscriptions.forEach { add(it) }
            }
        }

    override fun toString(): String {
        return fullName
    }

    // Nodes are always compared via identity.
    final override fun equals(other : Any?) : Boolean = super.equals(other)
    final override fun hashCode() : Int = super.hashCode()

    // endregion

}

// region Class-Level Metadata

/**
 * Class-level property providing the metadata for node-type,
 * which is initialised by the [Node.WithMetadata] annotation.
 */
val KClass<out Node<*>>.metadata : Node.Metadata by CachedReadOnlyProperty(
        cacheInitialiser = {
            val annotation = findAnnotation<Node.WithMetadata>()
                    ?: throw NodeMissingMetadataError(this)
            Node.Metadata(annotation.description)
        }
)

/**
 * Reified access to a node-type's metadata.
 *
 * @return      The metadata for the type of node.
 * @param N     The type of node.
 */
inline fun <reified N : Node<*>> getMetadataFor() : Node.Metadata {
    return N::class.metadata
}

// endregion
