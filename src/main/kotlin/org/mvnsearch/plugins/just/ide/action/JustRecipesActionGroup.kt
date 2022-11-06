package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.run.runJustCommand
import java.io.BufferedReader
import java.io.InputStreamReader


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
        val projectDir = project.guessProjectDir()!!
        val builder = ProcessBuilder()
        builder.command("just", "--color=never", "-l")
        builder.directory(projectDir.toNioPath().toFile())
        val process = builder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.lines().forEach {
            val recipeInfo = it.trim()
            if (recipeInfo.isNotEmpty() && !recipeInfo.startsWith("Available recipes:")) {
                val parts = recipeInfo.split('#', limit = 2)
                val recipeName = parts[0].trim()
                if (parts.size >= 2) {
                    val text = "$recipeName # ${parts[1].trim()}"
                    result.add(RunJustRecipeAction(recipeName, text))
                } else {
                    result.add(RunJustRecipeAction(recipeName, recipeName))
                }
            }
        }
        return result.toTypedArray()
    }
}

class RunJustRecipeAction(private val recipeName: String, private val text: String) : AnAction(text) {

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