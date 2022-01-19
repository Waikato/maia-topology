package maia.topology.node.base

import maia.topology.NodeConfiguration
import maia.topology.Node
import maia.topology.Topology
import maia.topology.io.Input

/**
 * TODO: What class does.
 * TODO: Implementation
 */
abstract class SubTopologyNode<C : NodeConfiguration>(block : C.() -> Unit = {}) : Node<C>(block) {

    private lateinit var subtopology : Topology

    abstract fun initialiseTopology() : Topology

    private fun ensureTopology() {
        if (!this::subtopology.isInitialized) subtopology = initialiseTopology()
    }

    fun getInput(name : String) : Input<*> {
        ensureTopology()
        TODO("return subtopology.disconnectedInputs")
    }

    override suspend fun main() {
        ensureTopology()
        subtopology.executeInScope(scope)
    }

}
