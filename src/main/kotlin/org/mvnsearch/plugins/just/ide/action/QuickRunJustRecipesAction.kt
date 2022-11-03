package org.mvnsearch.plugins.just.ide.action

import com.intellij.ide.actions.QuickSwitchSchemeAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import org.mvnsearch.plugins.just.ide.icons.JustIcons


class QuickRunJustRecipesAction : QuickSwitchSchemeAction(), DumbAware {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val p: Presentation = e.presentation
        val justfileAvailable = findJustfile(e.project!!) != null
        p.isEnabled = justfileAvailable
        p.isVisible = justfileAvailable
        p.icon = JustIcons.JUST_FILE
    }

    private fun findJustfile(currentProject: Project): String? {
        val projectDir = currentProject.guessProjectDir()
        if (projectDir != null) {
            if (projectDir.findChild("justfile") != null) {
                return "justfile"
            } else if (projectDir.findChild("Justfile") != null) {
                return "Justfile"
            }
        }
        return null
    }


    override fun fillActions(currentProject: Project, group: DefaultActionGroup, dataContext: DataContext) {
        val justfileName = findJustfile(currentProject)
        if (justfileName != null) {
            val actions = JustRecipesActionGroup(justfileName).getActions(dataContext, currentProject)
            actions.forEachIndexed { index, action ->
                if (index > 0) {
                    group.add(Separator())
                }
                group.add(action)
            }
        }
    }

    override fun getAidMethod(): ActionSelectionAid {
        return ActionSelectionAid.SPEEDSEARCH
    }


}