package maia.topology.io.error

/**
 * Exception for when an attempt is made to select over a collection
 * of zero inputs.
 */
class NoInputsForSelectException
    : InputException("Tried to select over 0 inputs")
