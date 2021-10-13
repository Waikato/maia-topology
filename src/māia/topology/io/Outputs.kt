package māia.topology.io

import māia.topology.Node
import kotlin.reflect.KProperty

/**
 * An array of outputs from a node.
 *
 * @param size      The number of outputs to create.
 * @param outputs   The outputs.
 * @param T         The value type of the outputs.
 */
class Outputs<T> private constructor(
        override val size : Int,
        private val outputs : ArrayList<Output<T>>
) : Throughput<Outputs<T>, T>(), List<Output<T>> by outputs {

    /**
     * Constructs an output array of the given size.
     *
     * @param size  The number of outputs to create.
     */
    constructor(size : Int) : this(size, ArrayList())

    init {
        // Make sure size isn't negative
        if (size < 0) throw IllegalArgumentException("size can't be negative (got $size)")

        // Create the actual output instances
        for (i in 0 until size) {
            outputs.add(SubOutput(i))
        }
    }

    override val isSubscribed: Boolean
        get() = outputs.any { it.isSubscribed }

    override fun onDelegation(
        owner : Node<*>,
        property : KProperty<*>,
        name : String
    ) {
        for (index in 0 until size) {
            val output = outputs[index]
            output.provideDelegate(owner, property)
            owner.registerOutput(output, output.name)
        }
    }

    override fun getValue() : Outputs<T> {
        return this
    }

    /**
     * A single output from a group of outputs.
     */
    inner class SubOutput<T>(val index : Int) : Output<T>() {

        override val name : String by lazy {
            "${this@Outputs.name}[$index]"
        }

        override val metadata : Metadata by lazy {
            Metadata("${this@Outputs.metadata.description} [index $index]")
        }

    }

}
