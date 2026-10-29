package org.mvnsearch.plugins.just.lang.resolve

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.mvnsearch.plugins.just.lang.lexer.JustLexerAdapter
import org.mvnsearch.plugins.just.lang.parser.JustParserDefinition
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes

class JustFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            JustLexerAdapter(),
            TokenSet.create(JustTypes.RECIPE_NAME, JustTypes.DEPENDENCY_NAME),
            JustParserDefinition.COMMENTS,
            JustParserDefinition.LITERALS
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is JustRecipeStatement
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return if (element is JustRecipeStatement) "recipe" else ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return (element as? JustRecipeStatement)?.name ?: ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return (element as? JustRecipeStatement)?.name ?: ""
    }
}
