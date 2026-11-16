package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.xml.util.XmlStringUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.mvnsearch.plugins.just.JUSTFILES_MARKDOWN
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.lang.psi.JustFileType
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

    private val recipeCache = ConcurrentHashMap<String, JustfileContents>()

    // Keep source paths across invalidation so edits during a reload still trigger a refresh.
    private val sourceDependencies = ConcurrentHashMap<String, Set<String>>()

    // failed loads are remembered briefly, so callers that reload on a cache miss (tree refresh, action update) don't rerun just in a loop
    private val failedLoads = ConcurrentHashMap<String, DumpFailure>()

    private val loadingFiles = ConcurrentHashMap.newKeySet<String>()

    /** True while a refresh still has undiscovered or uncached Justfiles, including loads not yet started. */
    fun hasPendingLoads(): Boolean {
        if (loadingFiles.isNotEmpty()) return true
        val files = justfiles ?: return true
        return files.any { getCachedContents(it) == null }
    }

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

    /**
     * true when the last lookup ran while indexes were not ready, so its (empty) result was not cached and should be retried after indexing
     */
    @Volatile
    var incompleteJustfiles = false
        private set

    fun findJustfiles(): List<VirtualFile> {
        // drop files invalidated by a VFS change the refresh listener has not caught
        justfiles?.let { cached -> if (cached.all { it.isValid }) return cached }

        val result = try {
            runReadAction {
                // getService instead of DumbService.getInstance/isDumb: on newer platforms those compile to DumbService.Companion, missing on older IDEs
                if (project.getService(DumbService::class.java).isDumb) throw IndexNotReadyException.create()
                // project scope only: excluded dirs (build, target, node_modules ...), library roots and ignored files are skipped
                val scope = GlobalSearchScope.projectScope(project)
                val files = FileTypeIndex.getFiles(JustFileType, scope).filter { Just.isJustfile(it.name) }
                // only files named README.md, AGENTS.md or CLAUDE.md are read to look for a just code block
                val markdownFiles = JUSTFILES_MARKDOWN.flatMap { FilenameIndex.getVirtualFilesByName(it, scope) }
                    .filter { Just.isJustfileMarkdown(it) }
                val discovered = (files + markdownFiles).filter { !it.isDirectory }.distinctBy { it.path }
                // The ignored file only affects tool-window discovery.
                val ignoreMatcher = project.guessProjectDir()?.let { JustIgnoreMatcher.fromProjectRoot(it) }
                if (ignoreMatcher == null) discovered else discovered.filterNot(ignoreMatcher::ignores)
            }
        } catch (_: IndexNotReadyException) {
            incompleteJustfiles = true
            return emptyList()
        }

        incompleteJustfiles = false
        justfiles = result

        // Remove dependencies for roots no longer included in tool-window discovery.
        val roots = result.mapTo(mutableSetOf()) { it.path }
        sourceDependencies.keys.removeIf { it !in roots }

        return result
    }

    // Preserve the original root-level recipe API used by JustRecipesActionGroup.
    fun getCachedRecipes(justVirtualFile: VirtualFile): List<RecipeNode>? = getCachedContents(justVirtualFile)?.recipes

    fun getCachedContents(justVirtualFile: VirtualFile): JustfileContents? {
        recipeCache[justVirtualFile.path]?.let { return it }
        return if (failedLoads[justVirtualFile.path]?.isRecent() == true) JustfileContents() else null
    }

    /** Imported and module files can use arbitrary filenames, and may be outside the project. */
    fun isDependencySource(path: String): Boolean {
        val normalized = JustSourceDependencyCollector.normalize(path)
        return sourceDependencies.values.any { normalized in it }
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

    fun loadRecipes(justVirtualFile: VirtualFile): List<RecipeNode> = loadContents(justVirtualFile).recipes

    private fun loadContents(justVirtualFile: VirtualFile): JustfileContents {
        getCachedContents(justVirtualFile)?.let { return it }
        val startGeneration = generation.get()
        // PSI discovery requires a read action; never perform it inside the VFS write-action listener.
        val dependencies = try {
            runReadAction { JustSourceDependencyCollector(project).collect(justVirtualFile) }
        } catch (_: Exception) {
            null // Keep the last known dependencies if indexing or an invalid file prevents discovery.
        }
        // Start watching newly discovered dependencies before executing just: edits made during
        // the dump must also invalidate the root. Keep old paths until a successful reload.
        if (dependencies != null && startGeneration == generation.get()) {
            sourceDependencies.merge(justVirtualFile.path, dependencies) { old, new -> old + new }
        }
        // run just outside computeIfAbsent: a long running process must not block other writes to the map
        val recipes = try {
            loadRecipesInternal(justVirtualFile)
        } catch (e: DumpException) {
            if (startGeneration == generation.get()) {
                // Dependencies remain watched even if the dump fails (e.g. a missing optional file).
                // failures are never written to recipeCache, so the next load after the retry delay runs just again
                val failure = DumpFailure(e.title, e.message)
                val previous = failedLoads.put(justVirtualFile.path, failure)
                // an invalidate between the check and the put would leave a stale failure behind
                if (startGeneration != generation.get()) failedLoads.remove(justVirtualFile.path, failure)
                // notify once per distinct error, the periodic retries of the same failure stay silent
                if (previous == null || previous.title != e.title || previous.message != e.message) {
                    notifyJustDumpFailed(e.title, e.message)
                }
            }
            return JustfileContents()
        }
        if (startGeneration != generation.get()) return recipes
        dependencies?.let { sourceDependencies[justVirtualFile.path] = it }
        failedLoads.remove(justVirtualFile.path)
        val cached = recipeCache.putIfAbsent(justVirtualFile.path, recipes)
        // an invalidate between the check and the put would leave stale recipes cached, drop them again (only if they are ours)
        if (cached == null && startGeneration != generation.get()) {
            recipeCache.remove(justVirtualFile.path, recipes)
        }
        return cached ?: recipes
    }

    /**
     * @throws DumpException when just cannot be executed, times out, exits with an error or prints invalid JSON
     */
    private fun loadRecipesInternal(justVirtualFile: VirtualFile): JustfileContents {
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

        return try {
            JustDumpTreeParser(project, justVirtualFile).parse(root)
        } catch (e: Exception) {
            throw DumpException(
                "Failed to read just --dump structure for ${justVirtualFile.name}",
                e.message ?: "Unexpected JSON structure",
            )
        }
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
