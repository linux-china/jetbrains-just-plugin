package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager

class JustManualRefreshAction(
    private val panel: JustToolWindowPanel
) : AnAction("Refresh", null, AllIcons.Actions.Refresh) {

    override fun actionPerformed(e: AnActionEvent) {
        FileDocumentManager.getInstance().saveAllDocuments()
        panel.refresh()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}