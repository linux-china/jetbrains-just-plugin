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
                // Match known Justfile names and resolved module sources without reading file content (VFS write action).
                val relevantEvents = events.filter { event ->
                    isDirectoryStructureChange(event) || eventFileNames(event).any { isCandidateName(it) }
                            || justService.isModuleSource(event.path)
                }
                if (relevantEvents.isEmpty()) return
                // A changed module can affect any root Justfile which imports it. Invalidate the full compiled tree.
                refresh(null)
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

    private fun isCandidateName(fileName: String): Boolean {
        return Just.isJustfile(fileName) || JUSTFILES_MARKDOWN.contains(fileName)
    }

    override fun dispose() = Unit
}
