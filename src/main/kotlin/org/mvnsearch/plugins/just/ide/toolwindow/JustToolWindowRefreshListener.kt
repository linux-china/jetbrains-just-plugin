package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.PathUtil
import org.mvnsearch.plugins.just.JUSTFILES_MARKDOWN
import org.mvnsearch.plugins.just.Just

class JustToolWindowRefreshListener(project: Project, private val refresh: (VirtualFile?) -> Unit) : Disposable {
    init {
        val justService = project.getService(JustfileService::class.java)
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                // Ignore-rule edits always re-run discovery, including content-only edits.
                if (events.any { JustIgnoreMatcher.FILE_NAME in eventFileNames(it) }) {
                    refresh(null)
                    return
                }

                // Imports and modules may use arbitrary names. Compare normalized paths only;
                // never parse or read files from this VFS write-action callback.
                val relevant = events.any { event ->
                    isDirectoryStructureChange(event) || eventFileNames(event).any(::isCandidateName) ||
                       eventPaths(event).any(justService::isDependencySource)
                }
                if (relevant) refresh(null) // A dependency can affect any compiled root Justfile.
            }
        })
    }

    // deleting, moving or renaming a directory may affect justfiles inside it, while the event only carries the directory name
    private fun isDirectoryStructureChange(event: VFileEvent): Boolean {
        val isStructureChange = event is VFileDeleteEvent || event is VFileMoveEvent
                || (event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME)
        return isStructureChange && event.file?.isDirectory == true
    }

    private fun eventFileNames(event: VFileEvent): List<String> {
        if (event is VFilePropertyChangeEvent) {
            if (event.propertyName != VirtualFile.PROP_NAME) return emptyList()
            return listOfNotNull(event.oldValue as? String, event.newValue as? String)
        }
        return listOf(PathUtil.getFileName(event.path))
    }

    /** Check both old and new paths for moves and renames, including removed dependency sources. */
    private fun eventPaths(event: VFileEvent): List<String> = when (event) {
        is VFileMoveEvent -> {
            val name = event.file?.name ?: PathUtil.getFileName(event.path)
            listOf(
                event.path,
                "${event.oldParent.path}/$name",
                "${event.newParent.path}/$name",
            )
        }
        is VFilePropertyChangeEvent -> if (event.propertyName == VirtualFile.PROP_NAME) {
            val parent = event.file?.parent?.path
            if (parent == null) listOf(event.path) else listOfNotNull(
                event.path,
                (event.oldValue as? String)?.let { "$parent/$it" },
                (event.newValue as? String)?.let { "$parent/$it" },
            )
        } else listOf(event.path)
        else -> listOf(event.path)
    }

    private fun isCandidateName(fileName: String): Boolean {
        return Just.isJustfile(fileName) || JUSTFILES_MARKDOWN.contains(fileName) || fileName == JustIgnoreMatcher.FILE_NAME
    }

    override fun dispose() = Unit
}
