package org.mvnsearch.plugins.just.lang.resolve

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement

class JustRecipeRenameInputValidator : RenameInputValidator {
    val justRecipePattern: ElementPattern<JustRecipeStatement> =
        PlatformPatterns.psiElement(JustRecipeStatement::class.java)

    override fun getPattern(): ElementPattern<out PsiElement?> {
        return justRecipePattern
    }

    override fun isInputValid(
        newName: String,
        element: PsiElement,
        context: ProcessingContext
    ): Boolean {
        return newName !in arrayOf(
            "alias",
            "set",
            "mod",
            "import",
            "if",
            "else"
        ) && newName.matches(Regex("[a-zA-Z_][a-zA-Z0-9_\\-]*"))
    }
}