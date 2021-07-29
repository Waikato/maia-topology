package māia.topology.io.error

import māia.topology.io.Input

/**
 * Base class for exceptions which are thrown in response to errors
 * to do with required inputs.
 *
 * @param requiredInput     The required input that is in error.
 * @param message           The error message.
 * @param cause             The exception which caused this error.
 */
open class RequiredInputException(
        val requiredInput : Input<*>,
        message : String? = null,
        cause : Throwable? = null
) : InputException(message, cause)
