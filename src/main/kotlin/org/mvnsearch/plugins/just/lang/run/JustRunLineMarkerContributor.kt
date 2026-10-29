package org.mvnsearch.plugins.just.lang.run

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.actions.runAnything.commands.RunAnythingCommandCustomizer
import com.intellij.ide.actions.runAnything.execution.RunAnythingRunProfile
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.execution.ParametersListUtil
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.JustBundle
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.ide.icons.JustIcons.JUST_FILE
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import org.mvnsearch.plugins.just.lang.run.JustRunConfiguration.Companion.adjustCommandLinePath
import javax.swing.Icon

@Suppress("DialogTitleCapitalization")
class JustRunLineMarkerContributor : RunLineMarkerProvider() {
    override fun getName(): String {
        return "run-just-recipe"
    }

    override fun getIcon(): Icon {
        return JustIcons.RUN_ICON
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        // register on the RECIPE_NAME leaf only, line markers must be attached to leaf elements
        if (psiElement.elementType != JustTypes.RECIPE_NAME) return null
        val recipeStatement = psiElement.parent as? JustRecipeStatement ?: return null
        val recipeName = psiElement.text
        var runIcon = JustIcons.RUN_ICON
        if (recipeName == "default") {
            runIcon = JustIcons.DEFAULT_RUN_ICON
        } else {
            recipeStatement.prevSibling?.prevSibling?.let {
                if (it.elementType == JustTypes.ATTRIBUTE) {
                    if (it.text == "[default]") {
                        runIcon = JustIcons.DEFAULT_RUN_ICON
                    }
                }
            }
        }
        return LineMarkerInfo(
            psiElement,
            psiElement.textRange,
            runIcon,
            {
                "Run just recipe: $recipeName"
            },
            { _, _ ->
                runJustRecipeByRunAnything(psiElement.project, psiElement, recipeName)
            },
            GutterIconRenderer.Alignment.CENTER,
            {
                recipeName
            }
        )
    }


    private fun runJustRecipeByRunAnything(project: Project, psiElement: PsiElement, taskName: String) {
        val justPsiFile = psiElement.containingFile as JustFile
        val justfile = justPsiFile.virtualFile
        val justCmdPath = Just.getJustCmdAbsolutionPath(project)
        val baseCommand = if (Just.isDefaultJustfile(justfile.name)) {
            listOf(justCmdPath, taskName)
        } else {
            listOf(justCmdPath, "-f", justfile.name, taskName)
        }
        val recipeStatement = justPsiFile.findRecipeElement(taskName) ?: return
        // pop dialog to ask input param value, and append it to command; abort if user cancelled
        val command = adjustCommandLine(project, recipeStatement, baseCommand) ?: return
        val workDirectory = if (justfile is VirtualFileWindow) {
            justfile.delegate.parent
        } else {
            justfile.parent
        }
        runJustCommand(
            project,
            workDirectory,
            justfile,
            command,
            SimpleDataContext.getProjectContext(project)
        )
    }


}

/**
 * Recipe parameter, name with `*`/`+` prefix for variadic parameters, defaultValue is the default expression text
 */
data class RecipeParam(val name: String, val defaultValue: String? = null) {
    val isVariadic: Boolean get() = name.startsWith('*') || name.startsWith('+')

    /** parameter can be omitted: has default value or `*` variadic */
    val isOptional: Boolean get() = defaultValue != null || name.startsWith('*')
}

fun adjustCommandLine(project: Project, recipeStatement: JustRecipeStatement, command: List<String>): List<String>? {
    val recipeParams = recipeStatement.params?.recipeParamList?.map {
        RecipeParam(it.recipeParamName.text, it.recipeParamValue?.text)
    } ?: emptyList()
    return appendRecipeArguments(project, recipeParams, command)
}

/**
 * pop dialog to ask input param value, and append it to command arguments; return null if user cancelled.
 * If an optional param is left empty, it and all following params are omitted, so just uses their default values.
 * Each value is passed as one argument as is, except variadic params, whose value is split like a shell command line.
 */
fun appendRecipeArguments(project: Project, params: List<RecipeParam>, command: List<String>): List<String>? {
    val newCommand = command.toMutableList()
    for (param in params) {
        val message = if (param.defaultValue != null) {
            "Please enter value for '${param.name}' (leave empty to use default: ${param.defaultValue}):"
        } else if (param.isOptional) {
            "Please enter value for '${param.name}' (optional):"
        } else {
            "Please enter value for '${param.name}':"
        }
        val value = Messages.showInputDialog(
            project,
            message,
            "Recipe Parameter: ${param.name}",
            null
        ) ?: return null // user cancelled
        if (value.isEmpty() && param.isOptional) {
            // just only allows optional params after an optional one, so skip the rest
            break
        }
        if (param.isVariadic) {
            newCommand += ParametersListUtil.parse(value)
        } else {
            newCommand += value
        }
    }
    return newCommand
}

fun runJustCommand(
    project: Project,
    workDirectory: VirtualFile,
    justfile: VirtualFile,
    command: List<String>,
    dataContext: DataContext
) {
    var commandDataContext = dataContext
    val projectDir = project.guessProjectDir()!!.path
    commandDataContext = RunAnythingCommandCustomizer.customizeContext(commandDataContext)
    val initialCommandLine = GeneralCommandLine(command)
        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        .withEnvironment("JUST_UNSTABLE", "1")
        .withEnvironment("PROJECT_DIR", projectDir)
        .withWorkDirectory(workDirectory.toNioPath().toFile())
    // adjust PATH
    adjustCommandLinePath(project, initialCommandLine)
    val commandLine =
        RunAnythingCommandCustomizer.customizeCommandLine(commandDataContext, workDirectory, initialCommandLine)
    try {
        val generalCommandLine =
            if (Registry.`is`("run.anything.use.pty", true)) PtyCommandLine(commandLine) else commandLine
        val runAnythingRunProfile = RunJustProfile(generalCommandLine, ParametersListUtil.join(command))
        ExecutionEnvironmentBuilder.create(project, DefaultRunExecutor.getRunExecutorInstance(), runAnythingRunProfile)
            .dataContext(commandDataContext)
            .buildAndExecute()
    } catch (e: ExecutionException) {
        Messages.showInfoMessage(project, e.message, JustBundle.message("run.anything.console.error.title"))
    }
}

class RunJustProfile(private val commandLine: GeneralCommandLine, private val originalCommand: String) :
    RunProfile {

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val handler = JustRunConfiguration.createJustProcessHandler(environment.project, commandLine)
                ProcessTerminatedListener.attach(handler)
                return handler
            }
        }
    }

    override fun getIcon(): Icon {
        return JUST_FILE
    }

    override fun getName(): String {
        return if (originalCommand.contains("/bin/just")) {
            originalCommand.substring(originalCommand.indexOf("/bin") + 5)
        } else {
            originalCommand
        }
    }
}