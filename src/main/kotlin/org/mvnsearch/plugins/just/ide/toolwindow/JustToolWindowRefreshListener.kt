package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.mvnsearch.plugins.just.Just

class JustToolWindowRefreshListener(project: Project, private val refresh: (VirtualFile?) -> Unit) : Disposable {
    init {
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                events.mapNotNull { it.file }
                    .filter { !it.isDirectory && (Just.isJustfile(it.name) || Just.isJustfileMarkdown(it)) }
                    .distinctBy { it.path }.forEach { refresh(it) }
            }
        })
    }

    override fun dispose() = Unit
}