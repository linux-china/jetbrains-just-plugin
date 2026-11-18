package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import java.awt.EventQueue
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath

/**
 * Keeps the user's expansion and selection choices across asynchronous tree model rebuilds.
 *
 * Unlike a TreeState snapshot captured at refresh time, these IDs are updated incrementally by
 * user gestures. A partially rebuilt tree cannot replace the desired state with an empty one.
 */
internal class JustTreeStateController(
    private val tree: Tree,
    private val model: AsyncTreeModel,
    private val nodeOf: (Any?) -> JustTreeNode?,
    private val invokeLaterIfAlive: (() -> Unit) -> Unit,
) : Disposable {
    private val expandedIds = mutableSetOf<String>()
    private var selectedId: String? = null
    // Expand All remains active while AsyncTreeModel discovers descendants after the toolbar action returns.
    private var expandAllRequested = false
    private var applyingState = false
    private var reconciliationScheduled = false
    private var disposed = false

    private val expansionListener = object : TreeExpansionListener {
        override fun treeExpanded(event: TreeExpansionEvent) = onExpansionChanged(event, true)
        override fun treeCollapsed(event: TreeExpansionEvent) = onExpansionChanged(event, false)
    }

    private val selectionListener = TreeSelectionListener {
        if (!applyingState && isUserGesture()) selectedId = nodeOf(tree.lastSelectedPathComponent)?.id
    }

    private val modelListener = object : TreeModelListener {
        override fun treeStructureChanged(event: TreeModelEvent) = scheduleReconciliation()
        override fun treeNodesChanged(event: TreeModelEvent) = scheduleReconciliation()
        override fun treeNodesInserted(event: TreeModelEvent) = scheduleReconciliation()
        override fun treeNodesRemoved(event: TreeModelEvent) = scheduleReconciliation()
    }

    init {
        tree.addTreeExpansionListener(expansionListener)
        tree.addTreeSelectionListener(selectionListener)
        model.addTreeModelListener(modelListener)
        scheduleReconciliation()
    }

    /** Called after invalidating StructureTreeModel; the desired state itself is never recaptured. */
    fun onRefresh() = scheduleReconciliation()

    /** Expand/collapse toolbar actions are explicit user choices, though they fire programmatic tree events. */
    fun expandAll() {
        // Don't snapshot visible paths: AsyncTreeModel has not necessarily loaded their descendants yet.
        // Reconciliation expands new children as they arrive and records each folder's stable ID.
        expandAllRequested = true
        applyingState = true
        try {
            TreeUtil.expandAll(tree)
        } finally {
            applyingState = false
            scheduleReconciliation()
        }
    }

    fun collapseAll() {
        expandAllRequested = false
        applyingState = true
        try {
            TreeUtil.collapseAll(tree, 0)
            expandedIds.clear()
        } finally {
            applyingState = false
            scheduleReconciliation()
        }
    }

    private fun onExpansionChanged(event: TreeExpansionEvent, expanded: Boolean) {
        if (applyingState || disposed) return
        val node = nodeOf(event.path.lastPathComponent) ?: return
        if (node is RootNode) return // The invisible root must stay expanded.

        // AsyncTreeModel also opens/closes paths while replacing nodes. Only user gestures change intent.
        if (isUserGesture()) {
            // A manual collapse supersedes Expand All; retain expansion state of other visible branches.
            if (!expanded) expandAllRequested = false
            if (expanded) expandedIds.add(node.id) else expandedIds.remove(node.id)
        }
        scheduleReconciliation()
    }

    private fun isUserGesture(): Boolean = when (EventQueue.getCurrentEvent()) {
        is InputEvent, is ActionEvent -> true
        else -> false
    }

    /** Coalesce model updates and run after JTree has reacted to the new model on the EDT. */
    private fun scheduleReconciliation() {
        if (disposed || reconciliationScheduled) return
        reconciliationScheduled = true
        invokeLaterIfAlive {
            reconciliationScheduled = false
            if (!disposed) reconcile()
        }
    }

    private fun reconcile() {
        val root = model.root ?: return
        val rootPath = TreePath(root)
        var selectedPath: TreePath? = null
        applyingState = true
        try {
            if (!tree.isExpanded(rootPath)) tree.expandPath(rootPath)

            // Visit only the invisible root and intended-open branches. Do not recursively load
            // collapsed modules, or interpret temporarily missing children as closed by the user.
            fun visit(path: TreePath) {
                val node = nodeOf(path.lastPathComponent) ?: return
                val shouldExpand = node is RootNode || expandAllRequested || node.id in expandedIds
                if (expandAllRequested && node !is RootNode && node !is RecipeNode) expandedIds.add(node.id)
                if (node !is RootNode) {
                    if (shouldExpand && !tree.isExpanded(path)) tree.expandPath(path)
                    if (!shouldExpand && tree.isExpanded(path)) tree.collapsePath(path)
                }
                if (node.id == selectedId) selectedPath = path
                if (!shouldExpand) return
                val parent = path.lastPathComponent
                for (index in 0 until model.getChildCount(parent)) {
                    visit(path.pathByAddingChild(model.getChild(parent, index)))
                }
            }

            visit(rootPath)
            if (selectedPath != null && nodeOf(tree.lastSelectedPathComponent)?.id != selectedId) {
                tree.selectionPath = selectedPath
            }
        } finally {
            applyingState = false
        }
    }

    override fun dispose() {
        disposed = true
        tree.removeTreeExpansionListener(expansionListener)
        tree.removeTreeSelectionListener(selectionListener)
        model.removeTreeModelListener(modelListener)
    }
}
