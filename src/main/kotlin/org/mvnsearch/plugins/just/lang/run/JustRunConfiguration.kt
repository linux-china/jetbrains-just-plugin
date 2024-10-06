package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.Icon


class JustRunConfiguration(
    project: Project, factory: ConfigurationFactory, name: String
) : RunConfigurationBase<JustRunConfigurationOptions>(project, factory, name) {

    fun getFileName(): String? {
        return options.getFileName()
    }

    fun setFileName(scriptName: String?) {
        options.setFileName(scriptName)
    }

    fun getRecipeName(): String? {
        return options.getRecipeName()
    }

    fun setRecipeName(recipeName: String?) {
        options.setRecipeName(recipeName)
    }

    fun getRecipeArgs(): String? {
        return options.getRecipeArgs()
    }

    fun setRecipeArgs(recipeArgs: String) {
        options.setRecipeArgs(recipeArgs)
    }

    fun getEnvVariables(): String? {
        return options.getEnvVariables()
    }

    fun setEnvVariables(envVariables: String) {
        options.setEnvVariables(envVariables)
    }

    fun getEnvVariablesAsMap(): Map<String, String> {
        val variables = getEnvVariables()
        if (variables != null && variables.contains('=')) {
            val variablesMap = mutableMapOf<String, String>()
            //pairs like NAME=xxx or NAME="a b c d"
            val p = Pattern.compile("(\\w+)=\"*((?<=\")[^\"]+(?=\")|(\\S+))\"*")
            val m: Matcher = p.matcher(variables)
            while (m.find()) {
                val name = m.group(1)
                val value = m.group(2)
                variablesMap[name.uppercase()] = value
            }
            return variablesMap
        }
        return emptyMap()
    }

    override fun getConfigurationEditor(): JustRunSettingsEditor {
        return JustRunSettingsEditor()
    }

    override fun getOptions(): JustRunConfigurationOptions {
        return super.getOptions() as JustRunConfigurationOptions
    }

    override fun getIcon(): Icon {
        return JustIcons.JUST_FILE
    }

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {
            override fun startProcess(): ProcessHandler {
                var justCmd = "just"
                val command = mutableListOf(justCmd)
                val fileName = getFileName()
                val recipeName = getRecipeName()
                val args = getRecipeArgs()
                if (!fileName.isNullOrEmpty()) {
                    command.add("-f")
                    command.add(fileName)
                }
                if (!recipeName.isNullOrEmpty()) {
                    command.add(recipeName)
                }
                if (!args.isNullOrEmpty()) {
                    command.addAll(ParametersListUtil.parse(args, false, true, false))
                }
                val commandLine = GeneralCommandLine(command)
                commandLine.workDirectory = File(project.basePath!!)
                commandLine.environment.putAll(getEnvVariablesAsMap())
                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine) as ColoredProcessHandler
                processHandler.setShouldKillProcessSoftly(true)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

}
