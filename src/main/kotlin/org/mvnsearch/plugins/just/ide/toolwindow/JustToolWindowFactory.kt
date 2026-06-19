package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class JustToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JustToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)

        toolWindow.contentManager.addContent(content)
    }
}
