package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiManager
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.run.runJustCommand


@SuppressWarnings("ComponentNotRegistered")
open class JustRecipesActionGroup(private val justfileName: String) : ActionGroup("Just", true), DumbAware {

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
        val result = mutableListOf<AnAction>()
        val justVirtualFile = project.guessProjectDir()!!.findChild(justfileName)!!
        val justfile = PsiManager.getInstance(project).findFile(justVirtualFile) as JustFile
        justfile.findAllRecipes().forEach {
            result.add(RunJustRecipeAction(it))
        }
        return result.toTypedArray()
    }
}

class RunJustRecipeAction(private val recipeName: String) : AnAction(recipeName) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val commandString = "${Just.getJustCmdAbsolutionPath()} $recipeName"
        val workDirectory = project.guessProjectDir()!!
        runJustCommand(project, workDirectory, commandString, e.dataContext)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = true
        e.presentation.isVisible = true
        e.presentation.icon = JustIcons.RUN_ICON
    }

}