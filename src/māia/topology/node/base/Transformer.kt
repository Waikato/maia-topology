package māia.topology.node.base

import māia.topology.NodeConfiguration
import māia.topology.io.Input
import māia.topology.io.Output
import māia.topology.io.Throughput
import māia.util.debugln

/**
 * Base class for nodes which read items from a primary input, and output
 * zero or more items per input on a primary output.
 *
 * @param block             The configuration block for the node's configuration.
 * @param maxConnections    The maximum number of connections allowed to the primary output.
 * @param C                 The type of configuration this node takes.
 * @param I                 The type of input item to the transformer.
 * @param O                 The type of item produced by the transformation.
 */
abstract class Transformer<C : NodeConfiguration, I, O>(
        block : C.() -> Unit = {},
        maxConnections : Int = -1
) : ContinuousLoopNode<C>(block), WithPrimaryInput<I>, WithPrimaryOutput<O> {

    /** The primary input to the transformer. */
    @Throughput.WithMetadata("The primary input")
    override val primaryInput by Input<I>()

    /** The primary output of the transformer. */
    @Throughput.WithMetadata("The primary output")
    override val primaryOutput by Output<O>(maxConnections)

    override fun loopCondition() : Boolean = !primaryOutput.isClosed

    final override suspend fun mainLoopInner() {
        // Get the input while we can
        val payload : I = primaryInput.pullOrAbort()

        // Log that we received an input
        debugln("$fullName got input $payload")

        // Transform the input
        val processed = transform(payload)

        // Output each transformed item
        while (!primaryOutput.isClosed && processed.hasNext()) {
            val single = processed.next()

            debugln("$fullName posted output $single")

            primaryOutput.push(single)
        }
    }

    /**
     * Transforms the input item into a number of output items.
     *
     * @param item  The input item to transform.
     * @return      An iterator over the output items resulting from the transformation.
     */
    abstract suspend fun transform(item : I) : Iterator<O>

}
