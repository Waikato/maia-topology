package maia.topology.io.util

/*
 * Defines extension functions for pushing to multiple outputs at once.
 */

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import maia.topology.io.Output

/**
 * Utility extension method on pairs of outputs which pushes an item
 * to each output simultaneously.
 *
 * @receiver        A pair of outputs.
 * @param item1     The value to push to the first output.
 * @param item2     The value to push to the second output.
 */
suspend fun <A, B> Pair<Output<A>, Output<B>>.multiPush(item1 : A, item2 : B) {
    coroutineScope {
        launch { first.push(item1) }
        launch { second.push(item2) }
    }
}

/**
 * Utility extension method on pairs of outputs which pushes an item
 * to each output simultaneously.
 *
 * @receiver        A pair of outputs.
 * @param items     A pair of items to push to the outputs.
 */
suspend fun <A, B> Pair<Output<A>, Output<B>>.multiPush(items: Pair<A, B>) {
    multiPush(items.first, items.second)
}

/**
 * Utility extension method on triples of outputs which pushes an item
 * to each output simultaneously.
 *
 * @receiver        A triple of outputs.
 * @param item1     The value to push to the first output.
 * @param item2     The value to push to the second output.
 * @param item3     The value to push to the third output.
 */
suspend fun <A, B, C> Triple<Output<A>, Output<B>, Output<C>>.multiPush(item1 : A, item2 : B, item3 : C) {
    coroutineScope {
        launch { first.push(item1) }
        launch { second.push(item2) }
        launch { third.push(item3) }
    }
}

/**
 * Utility extension method on triples of outputs which pushes an item
 * to each output simultaneously.
 *
 * @receiver        A triple of outputs.
 * @param items     A triple of items to push to the outputs.
 */
suspend fun <A, B, C> Triple<Output<A>, Output<B>, Output<C>>.multiPush(items : Triple<A, B, C>) {
    multiPush(items.first, items.second, items.third)
}
