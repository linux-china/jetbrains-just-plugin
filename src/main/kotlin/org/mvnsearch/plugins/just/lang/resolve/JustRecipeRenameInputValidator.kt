package org.mvnsearch.plugins.just.lang.resolve

import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext

import com.intellij.patterns.PlatformPatterns.psiElement
import org.mvnsearch.plugins.just.lang.psi.JustTypes.RECIPE_STATEMENT

class JustRecipeRenameInputValidator: RenameInputValidator {
    override fun getPattern(): ElementPattern<out PsiElement?> {
        return psiElement(RECIPE_STATEMENT)
    }

    override fun isInputValid(
        newName: String,
        element: PsiElement,
        context: ProcessingContext
    ): Boolean {
        return newName.matches(Regex("[a-zA-Z_][a-zA-Z0-9_\\-]*"))
    }
}