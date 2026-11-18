package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class JustTreeStructure(
    private val project: Project,
    private val onRecipesLoaded: () -> Unit,
) : AbstractTreeStructure() {
    private val justService = project.getService(JustfileService::class.java)
    private val root = RootNode(project)

    override fun getRootElement(): Any = root

    override fun getChildElements(element: Any): Array<Any> {
        return when (element) {
            is RootNode -> {
                val baseDir = project.guessProjectDir()

                // Keep Justfiles with modules but no root-level recipes visible.
                justService.findJustfiles()
                    .filter { !contentsOrLoadAsync(it).isEmpty() }
                    .map {
                        JustfileNode(project = project, file = it, relativePath = baseDir?.let { base ->
                            VfsUtilCore.getRelativePath(it.parent, base)?.takeIf { relativePath ->
                                relativePath.isNotEmpty()
                            }
                        })
                    }.sortedBy { it.file.path }.toTypedArray()
            }

            is JustfileNode -> {
                val contents = contentsOrLoadAsync(element.file)
                childrenOf(element.file, "", contents)
            }

            is ModuleNode -> childrenOf(element.file, element.modulePath, element.contents)

            is GroupNode -> {
                val contents = contentsOrLoadAsync(element.file)
                val recipes = if (element.modulePath.isEmpty()) contents.recipes
                else contents.moduleAt(element.modulePath)?.contents?.recipes.orEmpty()
                recipes.filter { it.getGroup() == element.group }.toTypedArray()
            }

            else -> emptyArray()
        }
    }

    private fun childrenOf(file: VirtualFile, modulePath: String, contents: JustfileContents): Array<Any> {
        val groups = contents.recipes.mapNotNull { it.getGroup() }.distinct().sorted().map {
            GroupNode(project = project, file = file, group = it, modulePath = modulePath)
        }
        return (groups + contents.recipes.filter { it.getGroup() == null } + contents.modules).toTypedArray()
    }

    /**
     * children are computed under a read action, so never run `just` here: return cached contents or load asynchronously
     */
    private fun contentsOrLoadAsync(file: VirtualFile): JustfileContents {
        return justService.getCachedContents(file) ?: run {
            justService.loadRecipesAsync(file, onRecipesLoaded)
            JustfileContents()
        }
    }

    private fun parentOfModule(file: VirtualFile, modulePath: String): Any {
        if (modulePath.isEmpty()) return JustfileNode(project, file, null)
        return justService.getCachedContents(file)?.moduleAt(modulePath) ?: JustfileNode(project, file, null)
    }

    override fun getParentElement(element: Any): Any? = when (element) {
        is JustfileNode -> root
        is ModuleNode -> parentOfModule(element.file, element.modulePath.substringBeforeLast("::", ""))
        is GroupNode -> parentOfModule(element.file, element.modulePath)
        is RecipeNode -> element.getGroup()?.let { GroupNode(project, element.file, it, element.modulePath) }
            ?: parentOfModule(element.file, element.modulePath)
        else -> null
    }

    override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> =
        JustNodeDescriptor(project, element as JustTreeNode, parentDescriptor)

    override fun commit() = Unit

    override fun hasSomethingToCommit() = false
}
