package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.Just
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class JustToolWindowService(
    private val project: Project,
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val recipeCache = ConcurrentHashMap<String, List<RecipeNode>>()

    @Volatile
    private var justfiles: List<VirtualFile>? = null

    fun invalidate(file: VirtualFile?) {
        if (file == null) {
            justfiles = null
            recipeCache.clear()
        } else {
            justfiles = null
            recipeCache.remove(file.path)
        }
    }

    fun findJustfiles(): List<VirtualFile> {
        justfiles?.let { return it }

        val root = project.guessProjectDir() ?: return emptyList()

        val result = mutableListOf<VirtualFile>()

        VfsUtilCore.iterateChildrenRecursively(root, null) {
            if (!it.isDirectory && Just.isJustfile(it.name)) {
                result += it
            }

            true
        }

        justfiles = result

        return result
    }

    fun loadRecipes(file: VirtualFile): List<RecipeNode> {
        return recipeCache.computeIfAbsent(file.path) {
            loadRecipesInternal(file)
        }
    }

    private fun loadRecipesInternal(file: VirtualFile): List<RecipeNode> {
        val process = ProcessBuilder(
            Just.getJustCmdAbsolutionPath(project),
            "--dump",
            "--dump-format",
            "json",
            "--justfile",
            file.path,
        ).directory(file.parent.toNioPath().toFile()).start()

        val output = process.inputStream.bufferedReader().readText()
        val errorOutput = process.errorStream.bufferedReader().readText()

        if (process.waitFor() != 0 || output.isBlank()) {
            notifyJustDumpFailed(file, errorOutput.ifBlank { output })
            return emptyList()
        }

        val root = try {
            json.parseToJsonElement(output).jsonObject
        } catch (e: Exception) {
            notifyJustDumpFailed(file, e.message ?: "Failed to parse just dump JSON")
            return emptyList()
        }

        val recipes = root["recipes"]?.jsonObject ?: return emptyList()

        return recipes.map { (name, recipeJson) ->
            val description = recipeJson.jsonObject["doc"]?.toString()?.removeSurrounding("\"")?.takeIf { it != "null" }

            RecipeNode(
                project = project,
                file = file,
                recipe = name,
                description = description,
            )
        }.sortedBy { it.recipe }
    }

    private fun notifyJustDumpFailed(file: VirtualFile, message: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("just toolwindow").createNotification(
            "Cannot load Just recipes",
            "Failed to parse ${file.name}:<br/><pre>${message.escapeHTML()}</pre>",
            NotificationType.WARNING
        ).notify(project)
    }
}