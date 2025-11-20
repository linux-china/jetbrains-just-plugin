package org.mvnsearch.plugins.just.lang.refactor

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement

class JustRecipeNamesValidator : RenameInputValidator {
    val justRecipePattern: ElementPattern<JustRecipeStatement> =
        PlatformPatterns.psiElement(JustRecipeStatement::class.java)

    override fun getPattern(): ElementPattern<out PsiElement?> {
        return justRecipePattern
    }

    override fun isInputValid(
        name: String,
        element: PsiElement,
        processingContext: ProcessingContext
    ): Boolean {
        return name !in arrayOf(
            "alias",
            "set",
            "mod",
            "import",
            "if",
            "else"
        ) && name.matches("[a-zA-Z_][a-zA-Z0-9_\\-]*".toRegex())
    }
}