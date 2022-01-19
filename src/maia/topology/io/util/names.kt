package maia.topology.io.util

import maia.topology.io.Throughput


/**
 * Joins the names of a collection of inputs/outputs into a string.
 *
 * @receiver                The collection of inputs/outputs.
 * @param andDescriptions   Whether to include descriptions as well.
 * @return                  A comma-separated string of names.
 */
fun Iterable<Throughput<*, *>>.joinNames(andDescriptions : Boolean = false) : String {
    return joinToString {
        if (andDescriptions)
            "'${it.name}' (${it.metadata.description})"
        else
            "'${it.name}'"
    }
}
