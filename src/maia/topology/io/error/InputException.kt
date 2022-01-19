package maia.topology.io.error

/**
 * Common base class for exceptions arising from the use of node inputs.
 *
 * @param message   The error message.
 * @param cause     The exception which caused this error.
 */
open class InputException(
        message : String? = null,
        cause : Throwable? = null
) : Exception(message, cause)
