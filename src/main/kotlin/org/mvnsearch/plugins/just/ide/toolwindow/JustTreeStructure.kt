package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore

class JustTreeStructure(
    private val project: Project,
) : AbstractTreeStructure() {
    private val justService = project.getService(JustfileService::class.java)
    private val root = RootNode(project)

    override fun getRootElement(): Any = root

    override fun getChildElements(element: Any): Array<Any> {
        return when (element) {
            is RootNode -> {
                val baseDir = project.guessProjectDir()

                justService.findJustfiles()
                    .filter { justService.loadRecipes(it).isNotEmpty() }
                    .map {
                        JustfileNode(project = project, file = it, relativePath = baseDir?.let { base ->
                        VfsUtilCore.getRelativePath(it.parent, base)?.takeIf { relativePath ->
                            relativePath.isNotEmpty()
                        }
                    })
                }.sortedBy { it.file.path }.toTypedArray()
            }

            is JustfileNode -> {
                val recipes = justService.loadRecipes(element.file)
                val groups = recipes.mapNotNull { it.getGroup() }.distinct().sorted().map {
                    GroupNode(project = project, file = element.file, group = it)
                }

                (groups + recipes.filter { it.getGroup() == null }).toTypedArray()
            }

            is GroupNode -> justService.loadRecipes(element.file).filter {
                it.getGroup() == element.group
            }.toTypedArray()

            else -> emptyArray()
        }
    }

    override fun getParentElement(element: Any): Any? = when (element) {
        is JustfileNode -> root
        is GroupNode -> JustfileNode(project, element.file, null)
        is RecipeNode -> element.getGroup()?.let { GroupNode(project, element.file, it) }
            ?: JustfileNode(project, element.file, null)

        else -> null
    }

    override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> =
        JustNodeDescriptor(project, element as JustTreeNode, parentDescriptor)

    override fun commit() = Unit

    override fun hasSomethingToCommit() = false
}