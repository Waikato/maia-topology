package māia.topology.node.base.error

import māia.topology.node.base.ContinuousLoopNode

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class ContinueOutsideOfMainLoop(cause : ContinuousLoopNode.ContinueNodeLoop)
    : Exception(
        "Attempted to call continue from outside of the node's main inner loop",
        cause
)
