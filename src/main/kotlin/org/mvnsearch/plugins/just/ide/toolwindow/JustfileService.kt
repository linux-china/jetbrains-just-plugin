package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.lang.run.RecipeParam
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

        val result = mutableListOf<VirtualFile>()

        // iterate project content only: excluded dirs (build, target, node_modules ...), library roots and ignored files are skipped
        runReadAction {
            ProjectFileIndex.getInstance(project).iterateContent {
                if (!it.isDirectory && (Just.isJustfile(it.name) || Just.isJustfileMarkdown(it))) {
                    result += it
                }
                true
            }
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
            val description = (recipeJson.jsonObject["doc"] as? JsonPrimitive)?.contentOrNull
            val params =
                recipeJson.jsonObject["parameters"]?.jsonArray?.filter { it.jsonObject["name"] != null }
                    ?.mapNotNull {
                        val name = (it.jsonObject["name"] as? JsonPrimitive)?.contentOrNull
                        val kind = (it.jsonObject["kind"] as? JsonPrimitive)?.contentOrNull
                        // default is a string for literal, or an expression array such as ["call", "arch"]
                        val defaultValue = when (val default = it.jsonObject["default"]) {
                            null, is JsonNull -> null
                            is JsonPrimitive -> default.content
                            else -> default.toString()
                        }
                        val paramName = when (kind) {
                            "plus" -> "+${name}"
                            "star" -> "*${name}"
                            else -> name
                        }
                        paramName?.let { RecipeParam(it, defaultValue) }
                    }
                    ?: emptyList()
            val attributes = recipeJson.jsonObject["attributes"]?.jsonArray?.mapNotNull {
                it
            }?: emptyList()
            RecipeNode(
                project = project,
                file = justVirtualFile,
                recipe = name,
                description = description,
                params = params,
                attributes = attributes,
                isPrivate = recipeJson.jsonObject["private"]?.toString() == "true",
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