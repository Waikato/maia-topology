package maia.topology

import maia.configure.Configuration
import maia.configure.ConfigurationElement
import maia.configure.ConfigurationItem

/**
 * Base configuration for nodes.
 *
 * @param defaultName   The default name for nodes using this configuration.
 */
open class NodeConfiguration(defaultName : String) : Configuration() {

    /**
     * Creates a named configuration with an empty default name.
     */
    constructor() : this("")

    /** The name of the node. */
    @ConfigurationElement.WithMetadata("The name of the node")
    var name by ConfigurationItem { defaultName }

}
