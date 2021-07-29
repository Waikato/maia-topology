package māia.topology.node.base

import māia.topology.NodeConfiguration
import māia.topology.io.Output
import māia.topology.io.Throughput
import māia.util.debugln

/**
 * Base class for nodes which generate items from some other
 * process than working with inputs.
 *
 * @param block             The configuration block for the node's configuration.
 * @param maxConnections    The maximum number of connections allowed to the primary output.
 * @param C                 The type of configuration this node takes.
 * @param T                 The type of item this source produces.
 */
abstract class Source<C : NodeConfiguration, T>(
        block : C.() -> Unit = {},
        maxConnections : Int = -1
) : ContinuousLoopNode<C>(block), WithPrimaryOutput<T> {

    /** The primary output of the source. */
    @Throughput.WithMetadata("The primary output")
    override val primaryOutput by Output<T>(maxConnections)

    final override fun loopCondition() : Boolean = !primaryOutput.isClosed

    final override suspend fun mainLoopInner() {
        // Produce a new item to output
        val payload = produce()

        debugln("$fullName produced item $payload")

        // Output the new item
        primaryOutput.push(payload)

        debugln("$fullName output $payload")
    }

    /**
     * Produces a new item to output.
     *
     * @return  The next item to output.
     */
    abstract suspend fun produce() : T

}
