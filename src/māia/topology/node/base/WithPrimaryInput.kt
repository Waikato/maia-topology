package māia.topology.node.base

import māia.topology.io.Input

/**
 * Interface for nodes which have a "main" input.
 *
 * @param T     The value type of the primary input.
 */
interface WithPrimaryInput<T> {

    /** The primary input. */
    val primaryInput : Input<T>

}
