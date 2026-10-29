package org.mvnsearch.plugins.just.lang.navigation

import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustModStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes

@Suppress("UnstableApiUsage")
class ModeNameAndPathRefNavigation : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        val elementType = element.elementType
        if (elementType == JustTypes.MOD_NAME || elementType == JustTypes.MOD_PATH || element.parent?.elementType == JustTypes.MOD_PATH) {
            val justFile = element.containingFile as? JustFile ?: return null
            if (!justFile.isPhysical) return null
            val modStatement = PsiTreeUtil.getParentOfType(element, JustModStatement::class.java) ?: return null
            return justFile.resolveModFile(modStatement)
        }
        return null
    }

}
