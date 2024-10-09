package org.mvnsearch.plugins.just.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.FoldingDescriptor.EMPTY
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement
import org.mvnsearch.plugins.just.lang.psi.JustVariableStatement

class JustfileFoldingBuilder : FoldingBuilderEx(), DumbAware {
    companion object {
        const val DEFAULT_PLACEHOLDER_TEXT = "..."
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is JustFile) {
            return EMPTY
        }
        return PsiTreeUtil.findChildrenOfAnyType(root, JustRecipeStatement::class.java, JustVariableStatement::class.java)
            .mapNotNull { statement ->
                if (statement is JustRecipeStatement) {
                    JustfileRecipeFoldingDescriptor(statement, statement.textRange)
                } else if (statement is JustVariableStatement) {
                    if (statement.text.trim().contains('\n')) {
                        JustfileVariableFoldingDescriptor(statement, statement.textRange)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return DEFAULT_PLACEHOLDER_TEXT
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}

class JustfileRecipeFoldingDescriptor(private val recipeStatement: JustRecipeStatement, textRange: TextRange) : FoldingDescriptor(recipeStatement, textRange) {
    override fun getPlaceholderText() = recipeStatement.recipeName.text + ":"
}

class JustfileVariableFoldingDescriptor(private val variableStatement: JustVariableStatement, textRange: TextRange) : FoldingDescriptor(variableStatement, textRange) {
    override fun getPlaceholderText() = variableStatement.variable.text + " := ..."
}