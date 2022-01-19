package maia.topology.error

/**
 * Exception for when an operation is performed on a topological
 * component that requires an execution context, but the containing
 * topology is not executing.
 */
class NotExecutingError(reason : String) : Exception(reason)
