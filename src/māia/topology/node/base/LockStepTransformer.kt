package māia.topology.node.base

import māia.topology.NodeConfiguration
import māia.util.itemIterator

/**
 * Base class for transformers that produce exactly one output item per
 * input item.
 *
 * @param block             The configuration block for the node's configuration.
 * @param maxConnections    The maximum number of connections allowed to the primary output.
 * @param C                 The type of configuration this node takes.
 * @param I                 The type of input item to the transformer.
 * @param O                 The type of item produced by the transformation.
 */
abstract class LockStepTransformer<C : NodeConfiguration, I, O>(
        block : C.() -> Unit = {},
        maxConnections : Int = -1
) : Transformer<C, I, O>(block, maxConnections) {

    final override suspend fun transform(item: I): Iterator<O> {
        // Return an iterator over the single result of transforming the item
        return itemIterator(transformSingle(item))
    }

    /**
     * Performs the actual transformation of the input item, under
     * the proviso that each input item yields a single output item.
     *
     * @param
     */
    abstract suspend fun transformSingle(item : I) : O
}
