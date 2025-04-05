package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.EnvVariablesTable
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.util.EnvironmentUtil
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.system.OS
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import java.io.File
import javax.swing.Icon


class JustRunConfiguration(
    project: Project, factory: ConfigurationFactory, name: String
) : RunConfigurationBase<JustRunConfigurationOptions>(project, factory, name) {

    fun getFileName(): String? {
        return options.getFileName()
    }

    fun setFileName(fileName: String?) {
        options.setFileName(fileName)
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
        if (variables.isNullOrEmpty()) {
            return emptyMap()
        }
        return EnvVariablesTable.parseEnvsFromText(variables)
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
                val justCmd = Just.getJustCmdAbsolutionPath(project)
                val command = mutableListOf(justCmd)
                val fileName = getFileName()
                val recipeName = getRecipeName()
                if (!fileName.isNullOrEmpty()) {
                    command.add("-f")
                    command.add(fileName)
                }
                if (!recipeName.isNullOrEmpty()) {
                    command.add(recipeName)
                }
                var args = getRecipeArgs()
                if (!args.isNullOrEmpty()) {
                    if (args.count { it == '$' } >= 2) {
                        val project = executionEnvironment.project
                        var module: com.intellij.openapi.module.Module? = null
                        project.guessProjectDir()?.let { projectDir ->
                            if (!fileName.isNullOrEmpty()) {
                                projectDir.findChild(fileName)?.let { justFile ->
                                    module = ModuleUtil.findModuleForFile(justFile, project)
                                }
                            } else {  // find default justfile
                                for (justFileName in listOf("justfile", "Justfile", ".justfile", ".Justfile")) {
                                    val child = projectDir.findChild(justFileName)
                                    if (child != null && child.exists()) {
                                        module = ModuleUtil.findModuleForFile(child, project)
                                        break
                                    }
                                }
                            }
                        }
                        args = ProgramParametersConfigurator().expandPathAndMacros(
                            args,
                            module,
                            project
                        )
                    }
                    if (!args.isNullOrEmpty()) {
                        command.addAll(ParametersListUtil.parse(args, false, true, false))
                    }
                }
                val commandLine = GeneralCommandLine(command)
                    .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                    .withEnvironment("JUST_UNSTABLE", "1")
                    .withWorkDirectory(project.guessProjectDir()!!.toNioPath().toFile())
                val envVariables = getEnvVariablesAsMap()
                if (envVariables.isNotEmpty()) {
                    commandLine.environment.putAll(envVariables)
                }
                // add project SDK bin directory to PATH
                injectProjectSdkIntoPath(project, commandLine)
                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine) as ColoredProcessHandler
                processHandler.setShouldKillProcessSoftly(true)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

    companion object {
        fun injectProjectSdkIntoPath(project: Project, commandLine: GeneralCommandLine) {
            val projectSdk = ProjectRootManager.getInstance(project).projectSdk
            if (projectSdk != null) {
                var homeDirectory = projectSdk.homeDirectory
                if (homeDirectory != null) {
                    val pair = getPathWithProjectSdk(homeDirectory)
                    commandLine.environment.put(pair.first, pair.second)
                }
            }
        }

        fun getPathWithProjectSdk(sdkHome: VirtualFile): Pair<String, String> {
            var homeDirectory = sdkHome
            if (homeDirectory.isFile) {
                homeDirectory = homeDirectory.parent!!
            }
            var pathEnvName = "PATH"
            if (OS.CURRENT == OS.Windows) {
                pathEnvName = "Path"
            }
            val pathVariable = EnvironmentUtil.getValue(pathEnvName)!!
            val binDir = homeDirectory.findChild("bin")
            return if (binDir != null && binDir.exists()) {
                pathEnvName to (binDir.path + File.pathSeparator + pathVariable)
            } else {
                pathEnvName to (homeDirectory.path + File.pathSeparator + pathVariable)
            }
        }
    }

}
