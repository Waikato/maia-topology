package māia.topology.visitation

/**
 * Allows a topology-visitor to visit a topology-visitable.
 *
 * @receiver            The topology-visitor.
 * @param visitable     The topology-visitable.
 * @return              The visitor (chainable).
 * @param TVr           The type of the visitor.
 * @param TVe           The type of the visitable.
 */
fun <TVr : TopologyVisitor, TVe : TopologyVisitable> TVr.visit(visitable : TVe) : TVr {
    // Defer to the reverse call
    visitable.visit(this)

    return this
}

/**
 * Allows a topology-visitable to be visited by a topology-visitor.
 *
 * @receiver        The topology-visitable.
 * @param visitor   The topology-visitor.
 * @return          The visitable (chainable).
 * @param TVe       The type of the visitable.
 * @param TVr       The type of the visitor.
 */
fun <TVe : TopologyVisitable, TVr : TopologyVisitor> TVe.visit(visitor : TVr) : TVe {
    // Begin the visitation
    visitor.begin()

    // Perform node-visitation first
    for (node in iterateNodes())
        visitor.node(node.type, node.configuration)

    // Perform subscription-visitation next
    for (subscription in iterateSubscriptions())
        visitor.subscription(
                subscription.fromNode,
                subscription.fromNodeOutputName,
                subscription.toNode,
                subscription.toNodeInputName
        )

    // Finalise the visitation
    visitor.end()

    return this
}
