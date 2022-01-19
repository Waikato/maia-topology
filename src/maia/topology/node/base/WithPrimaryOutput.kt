package maia.topology.node.base

import maia.topology.io.Output

/**
 * Interface for nodes which have a "main" output.
 *
 * @param T     The value type of the primary output.
 */
interface WithPrimaryOutput<T> {

    /** The primary output. */
    val primaryOutput : Output<T>

}
