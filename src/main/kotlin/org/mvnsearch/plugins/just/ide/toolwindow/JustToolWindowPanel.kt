package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.TreeExpander
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import org.mvnsearch.plugins.just.ide.action.RunJustRecipeAction
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class JustToolWindowPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {
    private val structure = JustTreeStructure(project)
    private val structureModel = StructureTreeModel(structure, this)
    private val asyncModel = AsyncTreeModel(structureModel, this)
    private val tree = Tree(asyncModel)

    init {
        tree.isRootVisible = false

        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        TreeUtil.installActions(tree)

        object : DoubleClickListener() {
            override fun onDoubleClick(event: java.awt.event.MouseEvent): Boolean {
                val path = tree.selectionPath ?: return false
                val last = path.lastPathComponent ?: return false

                val descriptor = when (last) {
                    is DefaultMutableTreeNode -> last.userObject
                    is NodeDescriptor<*> -> last
                    else -> return false
                }

                val node = when (descriptor) {
                    is JustNodeDescriptor -> descriptor.element
                    is RecipeNode -> descriptor
                    else -> null
                } as? RecipeNode ?: return false

                RunJustRecipeAction(node.recipe, node.recipe, node.file).actionPerformed(
                    JustToolWindowActionEventFactory.create(project)
                )

                return true
            }
        }.installOn(tree)

        val expander = object : TreeExpander {
            override fun expandAll() {
                TreeUtil.expandAll(tree)
            }

            override fun collapseAll() {
                TreeUtil.collapseAll(tree, 0)
            }

            override fun canExpand() = true
            override fun canCollapse() = true
        }

        val toolbar = ActionManager.getInstance().createActionToolbar("JustToolbar", DefaultActionGroup().apply {
            add(JustManualRefreshAction(this@JustToolWindowPanel))
            addSeparator()
            add(CommonActionsManager.getInstance().createExpandAllAction(expander, tree))
            add(CommonActionsManager.getInstance().createCollapseAllAction(expander, tree))
        }, true)

        toolbar.targetComponent = tree

        add(ScrollPaneFactory.createScrollPane(tree), BorderLayout.CENTER)
        add(toolbar.component, BorderLayout.NORTH)

        JustToolWindowRefreshListener(project, ::refresh)
    }

    fun refresh(file: VirtualFile? = null) {
        project.getService(JustToolWindowService::class.java).invalidate(file)

        ApplicationManager.getApplication().invokeLater {
            structureModel.invalidateAsync()
            tree.repaint()
        }
    }

    override fun dispose() = Unit
}