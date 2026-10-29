package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xml.util.XmlStringUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.lang.run.RecipeParam
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service(Service.Level.PROJECT)
class JustfileService(
    private val project: Project,
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val recipeCache = ConcurrentHashMap<String, List<RecipeNode>>()

    // failed loads are remembered briefly, so callers that reload on a cache miss (tree refresh, action update) don't rerun just in a loop
    private val failedLoads = ConcurrentHashMap<String, DumpFailure>()

    private val loadingFiles = ConcurrentHashMap.newKeySet<String>()

    // bumped on every invalidate, so a load started before the invalidation never caches stale recipes
    private val generation = AtomicLong()

    @Volatile
    private var justfiles: List<VirtualFile>? = null

    fun invalidate(file: VirtualFile?) {
        generation.incrementAndGet()
        if (file == null) {
            justfiles = null
            recipeCache.clear()
            failedLoads.clear()
        } else {
            justfiles = null
            recipeCache.remove(file.path)
            failedLoads.remove(file.path)
        }
    }

    fun findJustfiles(): List<VirtualFile> {
        // drop files invalidated by a VFS change the refresh listener has not caught
        justfiles?.let { cached -> if (cached.all { it.isValid }) return cached }

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

    fun getCachedRecipes(justVirtualFile: VirtualFile): List<RecipeNode>? {
        recipeCache[justVirtualFile.path]?.let { return it }
        return if (failedLoads[justVirtualFile.path]?.isRecent() == true) emptyList() else null
    }

    /**
     * load recipes on a pooled thread, outside any read action: running `just` synchronously under a read action is forbidden
     */
    fun loadRecipesAsync(justVirtualFile: VirtualFile, onLoaded: () -> Unit) {
        if (!loadingFiles.add(justVirtualFile.path)) return
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                loadRecipes(justVirtualFile)
            } finally {
                loadingFiles.remove(justVirtualFile.path)
            }
            if (!project.isDisposed) onLoaded()
        }
    }

    fun loadRecipes(justVirtualFile: VirtualFile): List<RecipeNode> {
        getCachedRecipes(justVirtualFile)?.let { return it }
        val startGeneration = generation.get()
        // run just outside computeIfAbsent: a long running process must not block other writes to the map
        val recipes = try {
            loadRecipesInternal(justVirtualFile)
        } catch (e: DumpException) {
            if (startGeneration == generation.get()) {
                // failures are never written to recipeCache, so the next load after the retry delay runs just again
                val previous = failedLoads.put(justVirtualFile.path, DumpFailure(e.title, e.message))
                // notify once per distinct error, the periodic retries of the same failure stay silent
                if (previous == null || previous.title != e.title || previous.message != e.message) {
                    notifyJustDumpFailed(e.title, e.message)
                }
            }
            return emptyList()
        }
        if (startGeneration != generation.get()) return recipes
        failedLoads.remove(justVirtualFile.path)
        return recipeCache.putIfAbsent(justVirtualFile.path, recipes) ?: recipes
    }

    /**
     * @throws DumpException when just cannot be executed, times out, exits with an error or prints invalid JSON
     */
    private fun loadRecipesInternal(justVirtualFile: VirtualFile): List<RecipeNode> {
        val commandLine = GeneralCommandLine(
            Just.getJustCmdAbsolutionPath(project),
            "--dump",
            "--dump-format",
            "json",
            "--justfile",
            justVirtualFile.path,
        ).withWorkDirectory(justVirtualFile.parent.path)
            .withCharset(StandardCharsets.UTF_8)

        // stdout and stderr are drained concurrently, and the process is killed on timeout
        val processOutput = try {
            ExecUtil.execAndGetOutput(commandLine, DUMP_TIMEOUT_MS)
        } catch (e: ExecutionException) {
            throw DumpException(
                "Failed to execute just for ${justVirtualFile.name}",
                (e.message ?: "Failed to execute just") + "\nCheck that just is installed and its path is configured.",
            )
        }

        if (processOutput.isTimeout) {
            throw DumpException(
                "just --dump timed out for ${justVirtualFile.name}",
                "just --dump did not finish within ${DUMP_TIMEOUT_MS / 1000}s",
            )
        }

        val output = processOutput.stdout
        if (processOutput.exitCode != 0 || output.isBlank()) {
            throw DumpException(
                "just --dump failed for ${justVirtualFile.name} (exit code ${processOutput.exitCode})",
                processOutput.stderr.ifBlank { output }.ifBlank { "no output" },
            )
        }

        val root = try {
            json.parseToJsonElement(output).jsonObject
        } catch (e: Exception) {
            throw DumpException(
                "Failed to parse just --dump output for ${justVirtualFile.name}",
                e.message ?: "Invalid JSON",
            )
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

    private fun notifyJustDumpFailed(title: String, message: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("just toolwindow").createNotification(
            "Cannot load Just recipes",
            "${XmlStringUtil.escapeString(title)}:<br/><pre>${XmlStringUtil.escapeString(message)}</pre>",
            NotificationType.WARNING
        ).notify(project)
    }

    private class DumpException(val title: String, override val message: String) : Exception(message)

    private class DumpFailure(val title: String, val message: String) {
        private val failedAt = System.currentTimeMillis()

        fun isRecent() = System.currentTimeMillis() - failedAt < FAILURE_RETRY_DELAY_MS
    }

    companion object {
        private const val DUMP_TIMEOUT_MS = 10_000
        private const val FAILURE_RETRY_DELAY_MS = 30_000
    }
}
