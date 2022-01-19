package maia.topology.io.util

import maia.topology.io.Output

/**
 * Whether both outputs are closed.
 */
val <A, B> Pair<Output<A>, Output<B>>.allClosed : Boolean
    get() {
        return first.isClosed && second.isClosed
    }

/**
 * Whether all three outputs are closed.
 */
val <A, B, C> Triple<Output<A>, Output<B>, Output<C>>.allClosed : Boolean
    get() {
        return first.isClosed && second.isClosed && third.isClosed
    }

/**
 * Whether all outputs in the collection are closed.
 */
val Collection<Output<*>>.allClosed : Boolean
    get() = isEmpty() || all { it.isClosed }
