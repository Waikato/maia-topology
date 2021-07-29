package māia.topology.io.error

import māia.topology.io.Input

/**
 * Exception for when an attempt is made to subscribe an input to
 * more than one output.
 *
 * @param input     The input which is over-subscribed.
 */
class InputAlreadySubscribedException(
        input : Input<*>
) : InputException("$input is already subscribed")
