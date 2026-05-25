package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project

object JustToolWindowActionEventFactory {
    fun create(project: Project): AnActionEvent = AnActionEvent.createFromDataContext(
        "JustToolWindow", Presentation(), SimpleDataContext.getProjectContext(project)
    )
}