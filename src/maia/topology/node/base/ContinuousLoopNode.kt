package maia.topology.node.base

import maia.topology.NodeConfiguration
import maia.topology.ExecutionState
import maia.topology.Node
import maia.topology.node.base.error.ContinueOutsideOfMainLoop

/**
 * Base class for standard nodes which implement a main loop,
 * allowing sub-types to stop the loop at the end of the current
 * iteration.
 *
 * @param block     The configuration block for the node's configuration.
 * @param C         The type of configuration this node takes.
 */
abstract class ContinuousLoopNode<C : NodeConfiguration>(
        block : C.() -> Unit = {}
) : Node<C>(block) {

    /** Whether the node should stop its main loop after this iteration. */
    private var shouldStop by ExecutionState { false }

    /**
     * Stops the node's main loop after this iteration.
     */
    protected fun stop() {
        shouldStop = true
    }

    /**
     * Cause the node's main loop to continue from the next iteration.
     */
    protected fun continueNodeLoop() : Nothing {
        throw ContinueNodeLoop()
    }

    final override suspend fun main() {
        // Perform setup
        try {
            preLoop()
        } catch (e : ContinueNodeLoop) {
            throw ContinueOutsideOfMainLoop(e)
        }

        // Repeat the inner section of the main loop until asked to stop
        while (!shouldStop && loopCondition()) {
            try {
                mainLoopInner()
            } catch (e : ContinueNodeLoop) {
                continue
            }
        }

        // Perform tear-down
        try {
            postLoop()
        } catch (e : ContinueNodeLoop) {
            throw ContinueOutsideOfMainLoop(e)
        }
    }

    /**
     * The loop-condition to evaluate before each iteration.
     */
    open fun loopCondition() : Boolean {
        return true
    }

    /**
     * The setup of the main loop.
     */
    open suspend fun preLoop() {}

    /**
     * The inner portion of the main loop.
     */
    abstract suspend fun mainLoopInner()

    /**
     * The tear-down of the main loop.
     */
    open suspend fun postLoop() {}

    /**
     * Exception which causes the node's loop to continue.
     */
    class ContinueNodeLoop : Exception()

}
