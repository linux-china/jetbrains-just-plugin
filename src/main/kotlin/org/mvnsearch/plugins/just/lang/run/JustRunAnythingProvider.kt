package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandLineProvider
import com.intellij.ide.actions.runAnything.commands.RunAnythingCommandCustomizer
import com.intellij.ide.actions.runAnything.items.RunAnythingItem
import com.intellij.ide.actions.runAnything.items.RunAnythingItemBase
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiManager
import com.intellij.util.execution.ParametersListUtil
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.psi.JustFile
import javax.swing.Icon

class JustRunAnythingProvider : RunAnythingCommandLineProvider() {


    override fun getIcon(value: String): Icon {
        return JustIcons.JUST_FILE
    }

    override fun getHelpGroupTitle(): String {
        return "Just"
    }

    override fun execute(dataContext: DataContext, value: String) {
        super.execute(dataContext, value)
    }

    override fun getCompletionGroupTitle(): String {
        return "Just"
    }

    override fun getHelpCommandPlaceholder(): String {
        return "just <taskName...>"
    }

    override fun getHelpCommand(): String {
        return "just"
    }


    override fun getMainListItem(dataContext: DataContext, value: String): RunAnythingItem {
        return RunAnythingItemBase(getCommand(value), JustIcons.JUST_FILE)
    }

    override fun run(dataContext: DataContext, commandLine: CommandLine): Boolean {
        val project = dataContext.getData(CommonDataKeys.PROJECT)!!
        val workDirectory = project.guessProjectDir()!!
        val commandString = Just.getJustCmdAbsolutionPath() + " " + commandLine.command
        val commandDataContext = RunAnythingCommandCustomizer.customizeContext(dataContext)
        var justColor = "auto"
        if (SystemInfo.isWindows) {
            justColor = "never"
        }
        val initialCommandLine = GeneralCommandLine(ParametersListUtil.parse(commandString, false, true))
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withEnvironment("JUST_COLOR", justColor)
            .withWorkDirectory(workDirectory.path)
        val newCommandLine =
            RunAnythingCommandCustomizer.customizeCommandLine(commandDataContext, workDirectory, initialCommandLine)
        try {
            val generalCommandLine =
                if (Registry.`is`("run.anything.use.pty", false)) PtyCommandLine(newCommandLine) else newCommandLine
            val runAnythingRunProfile = RunJustProfile(generalCommandLine, commandString)
            ExecutionEnvironmentBuilder.create(
                project,
                DefaultRunExecutor.getRunExecutorInstance(),
                runAnythingRunProfile
            )
                .dataContext(commandDataContext)
                .buildAndExecute()
        } catch (e: ExecutionException) {
            Messages.showInfoMessage(project, e.message, IdeBundle.message("run.anything.console.error.title"))
        }
        return true
    }

    override fun suggestCompletionVariants(dataContext: DataContext, commandLine: CommandLine): Sequence<String> {
        val project = dataContext.getData(CommonDataKeys.PROJECT)
        val projectDir = project?.guessProjectDir()
        var justfile = projectDir?.findChild("justfile")
        if (justfile == null) {
            justfile = projectDir?.findChild("Justfile")
        }
        if (justfile != null) {
            val psiFile = PsiManager.getInstance(project!!).findFile(justfile) as JustFile
            return psiFile.findAllRecipes().asSequence()

        }
        return emptySequence()
    }
}