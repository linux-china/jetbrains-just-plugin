package org.mvnsearch.plugins.just.lang.navigation

import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustModStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes

@Suppress("UnstableApiUsage")
class ModeNameAndPathRefNavigation : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (element.elementType == JustTypes.MOD_NAME) {
            val justFile = element.containingFile as JustFile
            if (justFile.isPhysical) {
                val modeName = element.text
                val modStatement = element.parent as JustModStatement
                var modFilePath = "$modeName.just"
                modStatement.modPath?.let { modePath ->
                    modFilePath = modePath.text.trim { it == '\"' || it == '\'' }
                }
                justFile.containingDirectory?.let { directory ->
                    directory.findFile(modFilePath)?.let {
                        return it
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