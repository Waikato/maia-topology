package maia.topology.io

import maia.topology.Node
import maia.topology.io.error.ThroughputMissingMetadataError
import maia.util.property.CachedReadOnlyProperty
import maia.util.property.SingleUseReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Base class for inputs/outputs to nodes, providing common functionality.
 */
abstract class Throughput<S : Throughput<S, T>, T> : SingleUseReadOnlyProperty<Node<*>, S>() {

    /** Whether this input/output is part of a connected subscription. */
    abstract val isSubscribed : Boolean

    override fun toString(): String {
        return "${this::class.simpleName} '$name' of $owner"
    }

    // region Metadata

    /** Instance accessor to the meta-data for this throughput. */
    open val metadata : Metadata
        get() = property.metadata

    /**
     * Property meta-data object for throughputs.
     */
    data class Metadata(
            val description : String
    )

    /**
     * Annotation which defines the metadata for a throughput.
     */
    @Target(AnnotationTarget.PROPERTY)
    annotation class WithMetadata(
            val description : String
    )

    // endregion

}

// region Property-Level Metadata

/**
 * Gets the meta-data of a throughput from its property.
 *
 * @throws ThroughputMissingMetadataError   If the property isn't annotated with meta-data.
 */
val KProperty<*>.metadata : Throughput.Metadata by CachedReadOnlyProperty(
        cacheInitialiser = {
            val annotation = findAnnotation<Throughput.WithMetadata>()
                    ?: throw ThroughputMissingMetadataError(this)
            Throughput.Metadata(annotation.description)
        }
)

// endregion
