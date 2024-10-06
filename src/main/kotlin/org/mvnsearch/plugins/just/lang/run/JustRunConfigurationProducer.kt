package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.lang.psi.JustTypes

class JustRunConfigurationProducer : LazyRunConfigurationProducer<JustRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return runConfigurationType<JustConfigurationType>().configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: JustRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val file = location.virtualFile ?: return false
        if (!isAcceptableFileType(file) || !file.isInLocalFileSystem) return false
        context.psiLocation?.let {
            val psiElement = it.originalElement
            if (psiElement.elementType == JustTypes.RECIPE_NAME) {
                val configurationName = getDefaultConfigurationName(file.name, psiElement.text)
                return configuration.name == configurationName
            }
        }
        return false
    }

    override fun setupConfigurationFromContext(
        configuration: JustRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val psiFile = sourceElement.get()?.containingFile ?: return false
        val virtualFile = psiFile.virtualFile ?: return false
        if (!isAcceptableFileType(virtualFile) || !virtualFile.isInLocalFileSystem) return false
        val psiLocation = context.psiLocation!!
        val originalElement = psiLocation.originalElement
        if (originalElement != null && originalElement.elementType == JustTypes.RECIPE_NAME) {
            val project = psiFile.project
            val filePath = virtualFile.path
            val projectBasePath = project.basePath!!
            if (filePath.startsWith(projectBasePath)) {
                configuration.setFileName(filePath.substring(projectBasePath.length + 1))
            } else {
                configuration.setFileName(filePath)
            }
            val recipeName = originalElement.text
            configuration.setRecipeName(recipeName)
            configuration.name = getDefaultConfigurationName(virtualFile.name, recipeName)
            return true
        }
        return false
    }

    private fun isAcceptableFileType(virtualFile: VirtualFile): Boolean {
        return Just.isJustfile(virtualFile.name)
    }

    private fun getDefaultConfigurationName(fileName: String, recipeName: String): String {
        return if (Just.isDefaultJustfile(fileName)) {
            "just - $recipeName"
        } else {
            "$fileName - $recipeName"
        }
    }

}