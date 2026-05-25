package org.mvnsearch.plugins.just.ide.action

import com.intellij.ide.actions.QuickSwitchSchemeAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import com.intellij.openapi.vfs.VirtualFile
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

    private fun findJustfile(currentProject: Project): VirtualFile? {
        val projectDir = currentProject.guessProjectDir() ?: return null

        setOf("justfile", "Justfile", ".justfile").forEach { child ->
            return projectDir.findChild(child)
        }

        return null
    }


    override fun fillActions(currentProject: Project, group: DefaultActionGroup, dataContext: DataContext) {
        val justfile = findJustfile(currentProject)
        if (justfile != null) {
            val actions = JustRecipesActionGroup(justfile).getActions(dataContext, currentProject)
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