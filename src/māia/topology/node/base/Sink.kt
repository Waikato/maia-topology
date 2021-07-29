package māia.topology.node.base

import māia.topology.NodeConfiguration
import māia.topology.io.Input
import māia.topology.io.Throughput
import māia.util.debugln

/**
 * Base class for nodes which terminate a branch of the topology,
 * consuming the items on their primary input in some way.
 *
 * @param block             The configuration block for the node's configuration.
 * @param C                 The type of configuration this node takes.
 * @param T                 The type of item this sink consumes.
 */
abstract class Sink<C : NodeConfiguration, T>(
        block : C.() -> Unit = {}
) : ContinuousLoopNode<C>(block), WithPrimaryInput<T> {

    /** The primary input of the sink. */
    @Throughput.WithMetadata("The primary input")
    override val primaryInput by Input<T>()

    final override suspend fun mainLoopInner() {
        // Get an item from the input while we can
        val item : T = primaryInput.pullOrAbort()

        debugln("$fullName got input $item")

        // Consume the item
        consume(item)

        debugln("$fullName consumed input $item")
    }

    /**
     * Consumes a single item from the input.
     *
     * @param item  The next item from the input.
     */
    abstract suspend fun consume(item : T)

}
