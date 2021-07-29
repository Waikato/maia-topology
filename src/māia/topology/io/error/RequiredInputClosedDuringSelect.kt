package māia.topology.io.error

import māia.topology.io.Input

/**
 * Exception for when a required input closes while partaking in
 * a select operation.
 *
 * @param requiredInput     The required input which closed.
 * @param inputs            The collection of inputs which were being selected from.
 */
class RequiredInputClosedDuringSelect(
        requiredInput : Input<*>,
        inputs : Collection<Input<*>>
) : RequiredInputException(
        requiredInput,
        "Required input $requiredInput closed while selecting from inputs: " +
                inputs.joinToString()
)
