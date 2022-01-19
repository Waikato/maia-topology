package maia.topology.visitation

import maia.configure.visitation.ConfigurationVisitable
import maia.topology.Node
import kotlin.reflect.KClass

/**
 * Interfaces for visitors of topology-like structures.
 */
interface TopologyVisitor {

    /**
     * Signals the beginning of a new visitation.
     */
    fun begin()

    /**
     * Called to supply the visitor with access to a representation of the
     * next node in the topology.
     *
     * @param type              The type of the next node.
     * @param configuration     A representation of the node's configuration.
     */
    fun node(type : KClass<out Node<*>>, configuration : ConfigurationVisitable)

    /**
     * Called to supply the visitor with access to a representation of the
     * next subscription in the topology.
     *
     * @param fromNode              The index of the node this subscription is
     *                              from in the iteration order.
     * @param fromNodeOutputName    The name of the output that the subscription
     *                              sources data from on the [fromNode].
     * @param toNode                The index of the node this subscription is
     *                              to in the iteration order.
     * @param toNodeInputName       The name of the input that the subscription
     *                              supplies data to on the [toNode].
     */
    fun subscription(
            fromNode : Int,
            fromNodeOutputName : String,
            toNode : Int,
            toNodeInputName : String
    )

    /**
     * Signals the end of a visitation.
     */
    fun end()

}
