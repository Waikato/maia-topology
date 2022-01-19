package maia.topology.visitation

import maia.configure.visitation.ConfigurationVisitable
import kotlin.reflect.KClass

/**
 * Interface for objects that can be visited in the same manner as
 * a topology.
 */
interface TopologyVisitable {

    /**
     * Iterates through all of the nodes in this topology, in a predefined
     * order.
     *
     * @return  An iterator of node representations.
     */
    fun iterateNodes() : Iterator<Node>

    /**
     * Iterates through all of the subscriptions in this topology.
     *
     * @return  An iterator of subscription representations.
     */
    fun iterateSubscriptions() : Iterator<Subscription>

    /**
     * Represents a single node in the topology.
     *
     * @param type              The type of node this is.
     * @param configuration     A representation of the node's configuration.
     */
    data class Node(
        val type : KClass<out maia.topology.Node<*>>,
        val configuration : ConfigurationVisitable
    )

    /**
     * Represents a single subscription between nodes in this topology.
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
    data class Subscription(
            val fromNode : Int,
            val fromNodeOutputName : String,
            val toNode : Int,
            val toNodeInputName : String
    )

}
