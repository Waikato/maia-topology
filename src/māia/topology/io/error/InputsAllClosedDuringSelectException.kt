package māia.topology.io.error

import māia.topology.io.Input

/**
 * Exception for when all inputs partaking in a select operation close.
 *
 * @param inputs    The inputs that were partaking in the select operation.
 */
class InputsAllClosedDuringSelectException(
        inputs : Collection<Input<*>>
) : InputException(
        "All of the following inputs closed during a select: " +
                inputs.joinToString()
)
