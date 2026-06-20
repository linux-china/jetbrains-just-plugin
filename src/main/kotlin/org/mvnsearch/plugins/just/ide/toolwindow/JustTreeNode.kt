package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import javax.swing.Icon

sealed class JustTreeNode(
    open val project: Project,
    open val id: String,
) {
    abstract val name: String
    abstract val icon: Icon
}

data class RootNode(
    override val project: Project,
) : JustTreeNode(project, "root") {
    override val name = "Just"
    override val icon = JustIcons.JUST_FILE
}

data class JustfileNode(
    override val project: Project,
    val file: VirtualFile,
    val relativePath: String?,
    override val id: String = file.url,
) : JustTreeNode(project, id) {
    override val name = file.name
    override val icon = JustIcons.JUST_FILE
}

data class RecipeNode(
    override val project: Project,
    val file: VirtualFile,
    val recipe: String,
    val description: String?,
    override val id: String = "${file.url}::$recipe",
    val params: List<String> = emptyList(),
    ) : JustTreeNode(project, id) {
    override val name = recipe
    override val icon = JustIcons.RUN_ICON
}