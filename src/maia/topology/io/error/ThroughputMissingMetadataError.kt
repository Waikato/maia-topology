package maia.topology.io.error

import kotlin.reflect.KProperty

/**
 * Exception for when an input/output is not annotated with meta-data.
 *
 * @param throughput    The property of the throughput that is missing meta-data.
 */
class ThroughputMissingMetadataError(
        throughput : KProperty<*>
) : Exception("No meta-data found for $throughput")
