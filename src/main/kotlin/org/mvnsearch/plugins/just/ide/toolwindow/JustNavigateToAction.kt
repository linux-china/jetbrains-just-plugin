package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.mvnsearch.plugins.just.JustBundle
import org.mvnsearch.plugins.just.lang.psi.JustFile

/** Navigation is explicit; double-click in the tree is reserved for running recipes. */
internal class JustNavigateToAction(
    private val project: Project,
    private val node: JustTreeNode,
) : DumbAwareAction(JustBundle.message("toolwindow.action.navigate.to")) {
    override fun actionPerformed(e: AnActionEvent) {
        if (project.isDisposed) return
        val target = ReadAction.compute<SourceLocation?, RuntimeException> {
            resolveLocation(project, node)
        } ?: return
        if (target.file.isValid) OpenFileDescriptor(project, target.file, target.offset).navigate(true)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}

private data class SourceLocation(val file: VirtualFile, val offset: Int = 0)

private fun resolveLocation(project: Project, node: JustTreeNode): SourceLocation? = when (node) {
    is JustfileNode -> SourceLocation(node.file)
    is ModuleNode -> moduleLocation(project, node.file, node.modulePath, node.sourcePath)
    is RecipeNode -> recipeLocation(project, node)
    is GroupNode -> {
        // Groups have no declaration of their own; navigate to the first recipe they contain.
        val contents = project.getService(JustfileService::class.java).getCachedContents(node.file)
        val recipes = if (node.modulePath.isEmpty()) contents?.recipes
        else contents?.moduleAt(node.modulePath)?.contents?.recipes
        val first = recipes?.firstOrNull { it.getGroup() == node.group }
        first?.let { recipeLocation(project, it) }
            ?: moduleLocation(project, node.file, node.modulePath)
    }
    is RootNode -> null
}

private fun recipeLocation(project: Project, node: RecipeNode): SourceLocation {
    val psi = moduleSourcePsi(project, node.file, node.modulePath)
        ?: modulePsi(project, node.file, node.modulePath)
    val declaration = psi?.findRecipeElement(node.recipe)
    val file = declaration?.containingFile?.virtualFile
    if (file != null) return SourceLocation(file, declaration.textOffset)
    // A recipe may not have a resolvable PSI declaration (e.g. an alias); open its containing Justfile.
    return moduleLocation(project, node.file, node.modulePath)
}

private fun moduleLocation(
    project: Project,
    root: VirtualFile,
    path: String,
    sourcePath: String? = null,
): SourceLocation {
    if (path.isEmpty()) return SourceLocation(root)
    val source = sourcePath?.let { resolveSource(root, it) }
        ?: project.getService(JustfileService::class.java).getCachedContents(root)
            ?.moduleAt(path)?.sourcePath?.let { resolveSource(root, it) }
        ?: modulePsi(project, root, path)?.virtualFile
    return SourceLocation(source ?: root)
}

private fun moduleSourcePsi(project: Project, root: VirtualFile, path: String): JustFile? {
    val source = project.getService(JustfileService::class.java).getCachedContents(root)
        ?.moduleAt(path)?.sourcePath?.let { resolveSource(root, it) } ?: return null
    return PsiManager.getInstance(project).findFile(source) as? JustFile
}

/** Resolve nested modules through their declarations, including declarations originating in imports. */
private fun modulePsi(project: Project, root: VirtualFile, path: String): JustFile? {
    var current = PsiManager.getInstance(project).findFile(root) as? JustFile ?: return null
    if (path.isEmpty()) return current
    for (name in path.split("::")) {
        val declaration = current.findModStatement(name) ?: return null
        current = (declaration.containingFile as? JustFile)?.resolveModFile(declaration) ?: return null
    }
    return current
}

private fun resolveSource(root: VirtualFile, sourcePath: String): VirtualFile? {
    val normalized = FileUtil.toSystemIndependentName(sourcePath)
    return if (FileUtil.isAbsolute(normalized)) {
        LocalFileSystem.getInstance().findFileByPath(normalized)
    } else {
        root.parent.findFileByRelativePath(normalized)
    }
}
