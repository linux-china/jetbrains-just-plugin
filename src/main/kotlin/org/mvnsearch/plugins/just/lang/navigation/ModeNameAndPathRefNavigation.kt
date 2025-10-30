package org.mvnsearch.plugins.just.lang.navigation

import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustModStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes

@Suppress("UnstableApiUsage")
class ModeNameAndPathRefNavigation : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (element.elementType == JustTypes.MOD_NAME) {
            val justFile = element.containingFile as JustFile
            if (justFile.isPhysical && justFile.containingDirectory != null) {
                val modeName = element.text
                val modStatement = element.parent as JustModStatement
                var modFilePath = "$modeName.just"
                modStatement.modPath?.let { modePath ->
                    modFilePath = modePath.text.trim { it == '\"' || it == '\'' }
                }
                val justfileVirtualDir = justFile.containingDirectory!!.virtualFile
                // If a module is named foo, just will search for the module file in foo.just,
                // foo/mod.just, foo/justfile, and foo/.justfile.
                val allModFiles = listOf(
                    modFilePath,
                    "${modeName}/mod.just",
                    "${modeName}/justfile",
                    "${modeName}/Justfile",
                    "${modeName}/.justfile",
                    "${modeName}/.Justfile"
                )
                for (modFile in allModFiles) {
                    justfileVirtualDir.findFileByRelativePath(modFile)?.let {
                        return PsiManager.getInstance(element.project).findFile(it)
                    }
                }
            }
        } else if (element.elementType == JustTypes.MOD_PATH) {
            val justFile = element.containingFile as JustFile
            if (justFile.isPhysical) {
                val modFilePath = element.text.trim { it == '\"' || it == '\'' }
                justFile.containingDirectory?.let { directory ->
                    directory.findFile(modFilePath)?.let {
                        return it
                    }
                }
            }
        }
        return null
    }

}