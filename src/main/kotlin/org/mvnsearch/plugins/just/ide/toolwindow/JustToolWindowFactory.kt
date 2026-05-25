package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.content.ContentFactory

private const val TOOLWINDOW_ID = "justfile"

class JustToolWindowFactory : ToolWindowFactory {
    override fun init(toolWindow: ToolWindow) {
        val project = (toolWindow as? ToolWindowEx)?.project ?: return

        @Suppress("UnstableApiUsage") StartupManager.getInstance(project).runAfterOpened {
            val manager = ToolWindowManager.getInstance(project)
            updateStripeButton(project, manager, toolWindow)
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JustToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)

        toolWindow.contentManager.addContent(content)
    }
}

private fun updateStripeButton(project: Project, manager: ToolWindowManager, toolWindow: ToolWindow) {
    manager.invokeLater {
        if (shouldDisableStripeButton(project, manager)) toolWindow.isShowStripeButton = false
    }
}

private fun shouldDisableStripeButton(project: Project, manager: ToolWindowManager): Boolean {
    @Suppress("UnstableApiUsage") val windowInfo = (manager as ToolWindowManagerEx).getLayout().getInfo(TOOLWINDOW_ID)

    if (windowInfo != null && windowInfo.isFromPersistentSettings) return false

    if (JustfileToolWindowStripeController.EP_NAME.extensionList.any {
            it.shouldHideStripeIconFor(project)
        }) return true

    return false
}