package maia.topology

import kotlinx.coroutines.*
import maia.configure.clone
import maia.topology.io.Input
import maia.topology.io.Output
import maia.topology.node.base.WithPrimaryInput
import maia.topology.node.base.WithPrimaryOutput
import maia.topology.visitation.TopologyVisitable
import maia.util.*
import maia.util.datastructure.OrderedHashSet
import maia.util.datastructure.OrderedSet
import maia.util.datastructure.buildOrderedSet

/**
 * An immutable topology of nodes and their connections.
 *
 * @param subscriptions     A dataset of subscriptions between nodes in the topology.
 */
class Topology(subscriptions : OrderedSet<Subscription>) : TopologyVisitable {

    // region Properties

    /** The set of all subscriptions in the topology. */
    val subscriptions : Set<Subscription> = buildOrderedSet { addAll(subscriptions) }

    /** The set of all inputs in the topology which are connected to an output. */
    val connectedInputs : Set<Input<out Any?>> = buildOrderedSet { addAll(subscriptions.map { it.to }) }

    /** The set of all outputs in the topology which are connected to at least one input. */
    val connectedOutputs : Set<Output<out Any?>> = buildOrderedSet { addAll(subscriptions.map { it.from }) }

    /** The set of all nodes in the topology. */
    val nodes : OrderedSet<Node<*>> = buildOrderedSet {
        addAll(connectedInputs.map { it.owner })
        addAll(connectedOutputs.map { it.owner })
    }

    /** The set of all inputs in the topology which are not connected to an output. */
    val disconnectedInputs : Set<Input<out Any?>>  = buildOrderedSet {
        addAll(nodes.flatMap { it.inputs.values }.filter { !connectedInputs.contains(it) })
    }

    /** The set of all outputs in the topology which are not connected to any inputs. */
    val disconnectedOutputs : Set<Output<out Any?>> = buildOrderedSet {
        addAll(nodes.flatMap { it.outputs.values }.filter { !connectedOutputs.contains(it) })
    }

    // endregion

    // region Execution

    /**
     * Executes this topology from a non-co-routine context, blocking until
     * the topology terminates.
     */
    fun execute() {
        runBlocking {
            executeInScope(this)
        }
    }

    /**
     * Executes this topology in the provided co-routine scope.
     *
     * @param scope     The co-routine scope to execute the topology in.
     */
    suspend fun executeInScope(scope : CoroutineScope) {
        // TODO: Ensure the topology is not already executing in this or some other scope

        // Reset all of the subscriptions
        for (subscription in subscriptions) subscription.reset()

        // Execute all of the nodes in the given scope
        for (node in nodes) node.execute(scope)
    }

    // endregion

    // region Visitation

    override fun iterateNodes() : Iterator<TopologyVisitable.Node> {
        return nodes.iterator().map {
            TopologyVisitable.Node(
                    it::class,
                    it.configuration
            )
        }
    }

    override fun iterateSubscriptions() : Iterator<TopologyVisitable.Subscription> {
        return subscriptions.iterator().map {
            TopologyVisitable.Subscription(
                    nodes.indexOf(it.from.owner),
                    it.from.name,
                    nodes.indexOf(it.to.owner),
                    it.to.name
            )
        }
    }

    // endregion

    /**
     * Helper class for building topologies.
     */
    class Builder() {

        /** The subscriptions added to the topology so far. */
        private val subscriptions = OrderedHashSet<Subscription>()

        private val nodeCopies = HashMap<Node<*>, Node<*>>()

        /**
         * Adds a subscription between an input and an output to the topology.
         *
         * @param input     The input to the connection.
         * @param output    The output from the connection.
         */
        fun <T> addSubscription(input : Input<in T>, output : Output<T>) {
            addSubscriptionUntyped(input, output)
        }

        /**
         * Adds a subscription between an input and an output to the topology,
         * without type-checking.
         *
         * @param input     The input to the connection.
         * @param output    The output from the connection.
         */
        internal fun addSubscriptionUntyped(input : Input<out Any?>, output : Output<out Any?>) {
            val inputCopy = nodeCopies.currentOrSet(input.owner) { input.owner.clone() }.inputs[input.name]!!
            val outputCopy = nodeCopies.currentOrSet(output.owner) { output.owner.clone() }.outputs[output.name]!!
            addSubscriptionDirect(inputCopy, outputCopy)
        }

        /**
         * Adds a subscription between an input and an output to the topology,
         * without type-checking, and without creating an owned copy. Should only be used
         * when the source nodes are given wholly to the topology.
         *
         * @param input     The input to the connection.
         * @param output    The output from the connection.
         */
        internal fun addSubscriptionDirect(input : Input<out Any?>, output : Output<out Any?>) {
            subscriptions.add(Subscription(output, input))
        }

        /**
         * Instantiates an actual topology from the state of the builder.
         *
         * @return  The constructed topology.
         */
         fun instantiate() : Topology {
            return Topology(subscriptions)
        }

        // region Syntactic sugar

        infix fun <T> Input<in T>.subscribesTo(output : Output<T>) {
            addSubscription(this, output)
        }

        infix fun <T> Input<in T>.subscribesTo(producer : WithPrimaryOutput<T>) {
            this subscribesTo producer.primaryOutput
        }

        infix fun <T> WithPrimaryInput<in T>.subscribesTo(output : Output<T>) {
            primaryInput subscribesTo output
        }

        infix fun <T> WithPrimaryInput<in T>.subscribesTo(producer : WithPrimaryOutput<T>) {
            primaryInput subscribesTo producer.primaryOutput
        }

        infix fun <Tr, I, O> Tr.subscribesTo(output : Output<I>) : Output<O>
                where Tr : WithPrimaryInput<in I>, Tr : WithPrimaryOutput<O> {
            primaryInput subscribesTo output
            return primaryOutput
        }

        infix fun <Tr, I, O> Tr.subscribesTo(producer : WithPrimaryOutput<I>) : Output<O>
                where Tr : WithPrimaryInput<in I>, Tr : WithPrimaryOutput<O> {
            return this subscribesTo producer.primaryOutput
        }

        // Use minus sign to connect inputs and outputs, and nodes with
        // primary inputs/outputs don't need to specify them at all

        operator fun <T> Output<T>.minus(input : Input<in T>) {
            addSubscription(input, this)
        }

        operator fun <T> WithPrimaryOutput<T>.minus(input: Input<in T>) {
            primaryOutput - input
        }

        operator fun <T> Output<T>.minus(sink : WithPrimaryInput<in T>) {
            this - sink.primaryInput
        }

        operator fun <T> WithPrimaryOutput<T>.minus(sink : WithPrimaryInput<in T>) {
            primaryOutput - sink.primaryInput
        }

        operator fun <Tr, I, O> Output<I>.minus(transformer : Tr) : Output<O>
                where Tr : WithPrimaryInput<in I>, Tr : WithPrimaryOutput<O> {
            this - transformer.primaryInput
            return transformer.primaryOutput
        }

        operator fun <Tr, I, O> WithPrimaryOutput<I>.minus(transformer : Tr) : Output<O>
                where Tr : WithPrimaryInput<in I>, Tr : WithPrimaryOutput<O> {
            return primaryOutput - transformer
        }

        // endregion

    }

}

/**
 * Builds a topology using the provided configuration block.
 *
 * @param block     The code for configuring the topology.
 * @return          The built topology.
 */
fun buildTopology(block : Topology.Builder.() -> Unit) : Topology {
    // Create a builder instance
    val builder = Topology.Builder()

    // Run the block
    builder.block()

    // Instantiate and return the topology
    return builder.instantiate()
}
