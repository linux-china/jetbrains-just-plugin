package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore

class JustTreeStructure(
    private val project: Project,
) : AbstractTreeStructure() {
    private val justService = project.getService(JustToolWindowService::class.java)
    private val root = RootNode(project)

    override fun getRootElement(): Any = root

    override fun getChildElements(element: Any): Array<Any> {
        return when (element) {
            is RootNode -> {
                val baseDir = project.guessProjectDir()

                justService.findJustfiles().map {
                    JustfileNode(project = project, file = it, relativePath = baseDir?.let { base ->
                        VfsUtilCore.getRelativePath(it.parent, base)?.takeIf { relativePath ->
                            relativePath.isNotEmpty()
                        }
                    })
                }.sortedBy { it.file.path }.toTypedArray()
            }

            is JustfileNode -> justService.loadRecipes(element.file).toTypedArray()

            else -> emptyArray()
        }
    }

    override fun getParentElement(element: Any): Any? = when (element) {
        is JustfileNode -> root
        is RecipeNode -> JustfileNode(project, element.file, null)
        else -> null
    }

    override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> =
        JustNodeDescriptor(project, element as JustTreeNode, parentDescriptor)

    override fun commit() = Unit

    override fun hasSomethingToCommit() = false
}