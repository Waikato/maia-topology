package maia.topology.error

import maia.topology.Node
import kotlin.reflect.KClass

/**
 * Error for when a sub-type of [Node] doesn't have a description.
 *
 * @param cls   The type of node.
 */
class NodeMissingMetadataError(
        cls : KClass<out Node<*>>
) : Exception("No meta-data found for node-type ${cls.qualifiedName}")
