package org.mvnsearch.plugins.just.lang.run

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandProvider
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import org.mvnsearch.plugins.just.parseRecipeName
import java.io.File
import javax.swing.Icon

@Suppress("DialogTitleCapitalization")
class JustRunLineMarkerContributor : RunLineMarkerProvider() {
    override fun getName(): String {
        return "run-just-recipe"
    }

    override fun getIcon(): Icon {
        return AllIcons.RunConfigurations.TestState.Run
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        val elementType = psiElement.elementType
        if (elementType == JustTypes.RECIPE_STATEMENT) {
            val text = psiElement.text
            val recipeName = parseRecipeName(text)
            return LineMarkerInfo(
                psiElement,
                psiElement.textRange,
                icon,
                { _: PsiElement? ->
                    "Run just recipe: $recipeName"
                },
                { e, elt ->
                    runJustRecipeByRunAnything(psiElement.project, psiElement, recipeName)
                },
                GutterIconRenderer.Alignment.CENTER
            )
        }
        return null
    }


    private fun runJustRecipeByRunAnything(project: Project, psiElement: PsiElement, taskName: String) {
        RunAnythingCommandProvider.runCommand(
            psiElement.containingFile.virtualFile.parent,
            "${getJustCmdAbsolutionPath()} $taskName",
            DefaultRunExecutor.getRunExecutorInstance(),
            SimpleDataContext.getProjectContext(project)
        )
    }

    private fun getJustCmdAbsolutionPath(): String {
        return if (SystemInfo.isWindows) {
            return "just"
        } else {
            if (File("/usr/local/bin/just").exists()) {
                "/usr/local/bin/just"
            } else {
                "just"
            }
        }
    }

}