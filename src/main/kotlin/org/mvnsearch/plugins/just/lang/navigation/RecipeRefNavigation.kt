package org.mvnsearch.plugins.just.lang.navigation

import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustTypes

@Suppress("UnstableApiUsage")
class RecipeRefNavigation : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (element.elementType == JustTypes.DEPENDENCY_NAME) {
            val dependencyName = element.text
            val justFile = element.containingFile as JustFile
            return justFile.findRecipeElement(dependencyName)
        }
        return null
    }

}