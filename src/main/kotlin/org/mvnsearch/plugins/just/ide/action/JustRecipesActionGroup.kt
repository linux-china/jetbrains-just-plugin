package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.ide.toolwindow.JustfileService
import org.mvnsearch.plugins.just.lang.run.runJustCommand


@SuppressWarnings("ComponentNotRegistered")
open class JustRecipesActionGroup(private val justfile: VirtualFile) : ActionGroup("Just", true), DumbAware {

    init {
        templatePresentation.icon = JustIcons.JUST_FILE
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return if (e != null) {
            getActions(e.dataContext, e.project!!)
        } else {
            arrayOf()
        }
    }

    open fun getActions(dataContext: DataContext?, project: Project): Array<AnAction> {
        return project.service<JustfileService>().loadRecipes(justfile)
            .filter { !it.isPrivate }
            .map {
                val text = if (it.description.isNullOrBlank()) it.recipe else "${it.recipe} # ${it.description}"
                RunJustRecipeAction(project, it.recipe, text, justfile, it.params)
            }
            .toTypedArray()
    }
}

class RunJustRecipeAction(
    private val project: Project,
    private val recipeName: String,
    private val text: String,
    private val virtualJustFile: VirtualFile? = null,
    private val params: List<String> = emptyList(),
) : AnAction(text) {
    override fun actionPerformed(e: AnActionEvent) {
        val justVirtualFile = virtualJustFile ?: e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val project = e.project!!
        val dataContext = e.dataContext
        val justCmdPath = Just.getJustCmdAbsolutionPath(project)
        val workingDirectory = justVirtualFile.parent
        var commandString = "$justCmdPath --justfile \"${justVirtualFile.name}\" $recipeName"
        // pop dialog to ask input param value, and append it to commandString
        if (params.isNotEmpty()) {
            for (paramName in params) {
                val value = Messages.showInputDialog(
                    project,
                    "Please enter value for '$paramName':",
                    "Recipe Parameter: $paramName",
                    null
                ) ?: return // user cancelled
                if (paramName.startsWith('*') || paramName.startsWith('+')) {
                    commandString += " $value"
                } else {
                    commandString += " \"$value\""
                }
            }
        }
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) {
                runJustCommand(project, workingDirectory, justVirtualFile, commandString, dataContext)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = true
        e.presentation.isVisible = true
        e.presentation.icon = JustIcons.RUN_ICON
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}