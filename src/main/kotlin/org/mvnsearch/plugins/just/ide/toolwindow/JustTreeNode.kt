package org.mvnsearch.plugins.just.ide.toolwindow

import kotlinx.serialization.json.JsonElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.run.RecipeParam
import javax.swing.Icon

sealed class JustTreeNode(
    open val project: Project,
    open val id: String,
) {
    abstract val name: String
    abstract val icon: Icon

    /**
     * nodes are identified by `id` only, so the nodes rebuilt in `JustTreeStructure.getParentElement` equal the real ones.
     * final, so the data classes below don't generate their own `equals`/`hashCode`
     */
    final override fun equals(other: Any?): Boolean =
        this === other || (other is JustTreeNode && other.javaClass == javaClass && other.id == id)

    final override fun hashCode(): Int = id.hashCode()
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

/** A compiled module contains its own recipes and zero or more nested modules. */
data class JustfileContents(
    val recipes: List<RecipeNode> = emptyList(),
    val modules: List<ModuleNode> = emptyList(),
) {
    fun isEmpty() = recipes.isEmpty() && modules.isEmpty()

    fun moduleAt(path: String): ModuleNode? {
        if (path.isEmpty()) return null
        var children = modules
        var current: ModuleNode? = null
        for (segment in path.split("::")) {
            current = children.firstOrNull { it.name == segment } ?: return null
            children = current.contents.modules
        }
        return current
    }
}

data class ModuleNode(
    override val project: Project,
    // Always the *root* Justfile: module recipes must be executed relative to it.
    val file: VirtualFile,
    val modulePath: String,
    val sourcePath: String?,
    val description: String?,
    val contents: JustfileContents,
    override val id: String = "${file.url}::module::$modulePath",
) : JustTreeNode(project, id) {
    override val name = modulePath.substringAfterLast("::")
    override val icon = JustIcons.MOD_ICON
}

data class GroupNode(
    override val project: Project,
    val file: VirtualFile,
    val group: String,
    val modulePath: String = "",
    override val id: String = "${file.url}::module::$modulePath::group::$group",
) : JustTreeNode(project, id) {
    override val name = group
    override val icon = JustIcons.GROUP_ICON
}

data class RecipeNode(
    override val project: Project,
    val file: VirtualFile,
    val recipe: String,
    val description: String?,
    val modulePath: String = "",
    override val id: String = "${file.url}::recipe::${if (modulePath.isEmpty()) recipe else "$modulePath::$recipe"}",
    val params: List<RecipeParam> = emptyList(),
    val attributes: List<JsonElement> = emptyList(),
    val isPrivate: Boolean = false,
) : JustTreeNode(project, id) {
    override val name = recipe
    override val icon = JustIcons.RUN_ICON

    val qualifiedRecipe: String
        get() = if (modulePath.isEmpty()) recipe else "$modulePath::$recipe"

    fun getGroup(): String? {
        val groupElement = attributes.find { it is JsonObject && it.jsonObject.containsKey("group") }
        val group = groupElement?.jsonObject?.get("group") as? JsonPrimitive
        return group?.contentOrNull?.takeIf { it.isNotBlank() }
    }
}
