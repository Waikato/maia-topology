package māia.topology.io

import māia.topology.Node
import kotlin.reflect.KProperty

/**
 * An array of inputs to a node.
 *
 * @param inputs    The inputs.
 * @param T         The value type of the inputs.
 */
class Inputs<T> private constructor(
        override val size : Int,
        private val inputs : ArrayList<Input<T>>
) : Throughput<Inputs<T>, T>(), List<Input<T>> by inputs {

    /**
     * Constructs an input array of the given size.
     *
     * @param size  The number of inputs to create.
     */
    constructor(size : Int) : this(size, ArrayList())

    // Create the actual input instances
    init {
        for (i in 0 until size) inputs.add(SubInput(i))
    }

    override val isSubscribed: Boolean
        get() = inputs.any { it.isSubscribed }

    override fun onDelegation(
        owner : Node<*>,
        property : KProperty<*>,
        name : String
    ) {
        for (index in 0 until size) {
            val input = inputs[index]
            input.provideDelegate(owner, property)
            owner.registerInput(input, input.name)
        }
    }

    override fun getValue() : Inputs<T> {
        return this
    }

    /**
     * A single input from a group of inputs.
     */
    inner class SubInput<T>(val index : Int) : Input<T>() {

        override val name : String by lazy {
            "${this@Inputs.name}[$index]"
        }

        override val metadata : Metadata by lazy {
            Metadata("${this@Inputs.metadata.description} [index $index]")
        }

    }

}
