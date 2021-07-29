package māia.topology.io.error

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import māia.topology.io.Input

/**
 * Exception for when an input closes during an attempt to pull a
 * value from it.
 *
 * @param input     The input which closed.
 * @param cause     The exception thrown by the underlying [Channel].
 */
class InputClosedDuringPullException(
        input : Input<*>,
        cause : ClosedReceiveChannelException
) : InputException("$input closed while attempting pull", cause)
