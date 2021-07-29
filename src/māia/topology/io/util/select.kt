package māia.topology.io.util

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import māia.topology.io.Input
import māia.topology.io.error.*
import māia.util.debugln

/**
 * Utility extension method to collections of inputs which returns the
 * first input to receive a value.
 *
 * @receiver                                        A collection of inputs to select from.
 * @param required                                  The set of required inputs, which will cause the select
 *                                                  to fail if they are missing or closed.
 * @return                                          A pair of the input that received a value,
 *                                                  and the value it received.
 * @throws NoInputsForSelectException               If the input collection is empty.
 * @throws InputsAllClosedDuringSelectException     If all inputs closed before a value was selected.
 */
suspend fun <T> Collection<Input<out T>>.select(
        required : Set<Input<out T>>? = null
) : Pair<Input<out T>, T> {
    // If the collection is empty, it's an error
    if (isEmpty()) throw NoInputsForSelectException()

    // Make sure all required inputs are present
    if (required != null) {
        for (input in required) {
            if (input !in this) throw RequiredInputMissingFromSelect(input, this)
        }
    }

    // Perform the select over these inputs
    try {
        return kotlinx.coroutines.selects.select {
            this@select.forEach { input ->
                val subscription = input.subscription
                if (subscription != null) {
                    subscription.channel.onReceive { value ->
                        debugln("$input selected...")
                        Pair(input, value as T)
                    }
                }
            }
        }

    // If an input closed during selection, reselect from the remaining unclosed inputs
    } catch (e : ClosedReceiveChannelException) {
        val unclosedInputs = filter { !it.isClosed }
        try {
            return unclosedInputs.select(required)
        } catch (e2 : RequiredInputException) {
            throw RequiredInputClosedDuringSelect(e2.requiredInput, this)
        } catch (e2 : InputException) {
            throw InputsAllClosedDuringSelectException(this)
        }
    }
}
