package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.ide.toolwindow.JustfileService
import org.mvnsearch.plugins.just.ide.toolwindow.RecipeNode
import org.mvnsearch.plugins.just.lang.run.RecipeParam
import org.mvnsearch.plugins.just.lang.run.appendRecipeArguments
import org.mvnsearch.plugins.just.lang.run.runJustCommand


@SuppressWarnings("ComponentNotRegistered")
open class JustRecipesActionGroup(private val justfile: VirtualFile) : ActionGroup("Just", true), DumbAware {

    init {
        templatePresentation.icon = JustIcons.JUST_FILE
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return arrayOf()
        // called during action update (under read action): never run `just` here, load it in background instead
        val justfileService = project.service<JustfileService>()
        val recipes = justfileService.getCachedRecipes(justfile)
        if (recipes == null) {
            justfileService.loadRecipesAsync(justfile) {}
            return arrayOf()
        }
        return toActions(project, recipes)
    }

    /**
     * get recipe actions, called on EDT: load recipes under a modal progress when the cache is empty
     */
    open fun getActions(dataContext: DataContext?, project: Project): Array<AnAction> {
        val justfileService = project.service<JustfileService>()
        val recipes = justfileService.getCachedRecipes(justfile)
            ?: ProgressManager.getInstance().runProcessWithProgressSynchronously<List<RecipeNode>, RuntimeException>(
                { justfileService.loadRecipes(justfile) }, "Loading Just Recipes", false, project
            )
        return toActions(project, recipes)
    }

    private fun toActions(project: Project, recipes: List<RecipeNode>): Array<AnAction> {
        return recipes
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
    private val params: List<RecipeParam> = emptyList(),
) : AnAction(text) {
    override fun actionPerformed(e: AnActionEvent) {
        val justVirtualFile = virtualJustFile ?: e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val project = e.project!!
        val dataContext = e.dataContext
        val justCmdPath = Just.getJustCmdAbsolutionPath(project)
        val workingDirectory = justVirtualFile.parent
        val baseCommand = listOf(justCmdPath, "--justfile", justVirtualFile.name, recipeName)
        // pop dialog to ask input param value, and append it to command; abort if user cancelled
        val command = appendRecipeArguments(project, params, baseCommand) ?: return
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) {
                runJustCommand(project, workingDirectory, justVirtualFile, command, dataContext)
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