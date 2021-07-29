package māia.topology.io.error

/**
 * Exception for when an output has a limited number of subscriptions allowed,
 * and an attempt is made to add another subscription.
 *
 * @param numConnections    The maximum number of allowed subscriptions.
 */
class FullyConnectedError(
        numConnections : Int
): Exception("Output already has $numConnections subscriptions, the maximum allowed.")
