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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.Just
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class JustfileService(
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
            if (!it.isDirectory) {
                if (Just.isJustfile(it.name)) {
                    result += it
                } else if (Just.isJustfileMarkdown(it)) {
                    result += it
                }
            }

            true
        }

        justfiles = result

        return result
    }

    fun loadRecipes(justVirtualFile: VirtualFile): List<RecipeNode> {
        return recipeCache.computeIfAbsent(justVirtualFile.path) {
            loadRecipesInternal(justVirtualFile)
        }
    }

    private fun loadRecipesInternal(justVirtualFile: VirtualFile): List<RecipeNode> {
        val process = ProcessBuilder(
            Just.getJustCmdAbsolutionPath(project),
            "--dump",
            "--dump-format",
            "json",
            "--justfile",
            justVirtualFile.path,
        ).directory(justVirtualFile.parent.toNioPath().toFile()).start()

        val output = process.inputStream.bufferedReader().readText()
        val errorOutput = process.errorStream.bufferedReader().readText()

        if (process.waitFor() != 0 || output.isBlank()) {
            notifyJustDumpFailed(justVirtualFile, errorOutput.ifBlank { output })
            return emptyList()
        }

        val root = try {
            json.parseToJsonElement(output).jsonObject
        } catch (e: Exception) {
            notifyJustDumpFailed(justVirtualFile, e.message ?: "Failed to parse just dump JSON")
            return emptyList()
        }

        val recipes = root["recipes"]?.jsonObject ?: return emptyList()

        return recipes.map { (name, recipeJson) ->
            val description = recipeJson.jsonObject["doc"]?.toString()?.removeSurrounding("\"")?.takeIf { it != "null" }
            val params =
                recipeJson.jsonObject["parameters"]?.jsonArray?.filter { it.jsonObject["name"] != null }
                    ?.mapNotNull {
                        val name = it.jsonObject["name"]?.toString()?.trim('"')
                        val kind = it.jsonObject["kind"]?.toString()?.trim('"')
                        when (kind) {
                            "plus" -> "+${name}"
                            "star" -> "*${name}"
                            else -> name
                        }
                    }
                    ?: emptyList()
            RecipeNode(
                project = project,
                file = justVirtualFile,
                recipe = name,
                description = description,
                params = params,
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