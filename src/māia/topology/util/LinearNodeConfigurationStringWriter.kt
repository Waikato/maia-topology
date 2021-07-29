package māia.topology.util

import māia.configure.Configuration
import māia.configure.ConfigurationElement
import māia.configure.visitation.ConfigurationVisitor
import java.util.*
import kotlin.reflect.KClass

/**
 * Writes a configuration to a one-line string.
 */
class LinearNodeConfigurationStringWriter : ConfigurationVisitor {

    /** The stack of nested sub-configuration name qualifiers. */
    private val subConfigurationStack = Stack<String>()

    /** The builder used to construct the string. */
    private val builder = StringBuilder()

    override fun begin(cls : KClass<out Configuration>) {
        // Clear the previous state
        subConfigurationStack.clear()
        builder.clear()

        // Put an empty name qualifier on the stack
        subConfigurationStack.push("")
    }

    override fun item(name : String, value : Any?, metadata : ConfigurationElement.Metadata) {
        // Add a comma after the last item, if this isn't the first item
        if (builder.isNotEmpty()) builder.append(", ")

        // Add the configuration item to the string
        builder.append("${fullName(name)} (${metadata.description}) = $value")
    }

    override fun beginSubConfiguration(name : String, cls : KClass<out Configuration>, metadata : ConfigurationElement.Metadata) {
        // Put the sub-configuration name qualifier on the stack
        subConfigurationStack.push("${subConfigurationStack.peek()}${name}.")
    }

    override fun endSubConfiguration() {
        // Remove the sub-configuration name qualifier from the stack
        subConfigurationStack.pop()
    }

    override fun end() {
        // Remove the final empty qualifier from the stack
        subConfigurationStack.pop()
    }

    override fun toString() : String {
        return builder.toString()
    }

    /**
     * Gets the fully-qualified name of a configuration item, including
     * sub-configuration prefixes.
     *
     * @param name  The name of the configuration item.
     * @return      The fully-qualified name of the configuration item.
     */
    private fun fullName(name : String) : String {
        return "${subConfigurationStack.peek()}${name}"
    }

}
