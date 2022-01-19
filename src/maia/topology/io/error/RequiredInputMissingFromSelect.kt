package maia.topology.io.error

import maia.topology.io.Input

/**
 * Exception for when a required input is missing from the collection
 * of inputs partaking in a select operation.
 *
 * @param requiredInput     The required input which is missing.
 * @param inputs            The collection of inputs from which it is missing.
 */
class RequiredInputMissingFromSelect(
        requiredInput : Input<*>,
        inputs : Collection<Input<*>>
) : RequiredInputException(
        requiredInput,
        "Required input $requiredInput missing from select inputs: " +
                inputs.joinToString()
)
