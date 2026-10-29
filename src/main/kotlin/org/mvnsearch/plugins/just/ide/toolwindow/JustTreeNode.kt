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

data class GroupNode(
    override val project: Project,
    val file: VirtualFile,
    val group: String,
    override val id: String = "${file.url}::group::$group",
) : JustTreeNode(project, id) {
    override val name = group
    override val icon = JustIcons.GROUP_ICON
}

data class RecipeNode(
    override val project: Project,
    val file: VirtualFile,
    val recipe: String,
    val description: String?,
    override val id: String = "${file.url}::$recipe",
    val params: List<RecipeParam> = emptyList(),
    val attributes: List<JsonElement> = emptyList(),
    val isPrivate: Boolean = false,
) : JustTreeNode(project, id) {
    override val name = recipe
    override val icon = JustIcons.RUN_ICON

    fun getGroup(): String? {
        val groupElement = this.attributes.find { it is JsonObject && it.jsonObject.containsKey("group") }
        val group = groupElement?.jsonObject?.get("group") as? JsonPrimitive
        return group?.contentOrNull?.takeIf { it.isNotBlank() }
    }
}