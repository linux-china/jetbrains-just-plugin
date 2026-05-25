package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes

class JustNodeDescriptor(
    project: Project,
    private val node: JustTreeNode,
    parentDescriptor: NodeDescriptor<*>?,
) : PresentableNodeDescriptor<JustTreeNode>(project, parentDescriptor) {
    override fun update(presentation: PresentationData) {
        presentation.clearText()
        presentation.setIcon(node.icon)

        when (node) {
            is JustfileNode -> {
                presentation.addText(node.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                node.relativePath?.let { presentation.addText(" $it", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
            }

            is RecipeNode -> {
                presentation.addText(node.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                node.description?.let { presentation.addText(" $it", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
            }

            else -> presentation.addText(node.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    override fun getElement(): JustTreeNode = node

    override fun equals(other: Any?): Boolean = other is JustNodeDescriptor && other.element.id == element.id

    override fun hashCode(): Int = element.id.hashCode()
}