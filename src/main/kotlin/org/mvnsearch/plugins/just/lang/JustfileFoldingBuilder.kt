package org.mvnsearch.plugins.just.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.FoldingDescriptor.EMPTY_ARRAY
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
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
            return EMPTY_ARRAY
        }
        return PsiTreeUtil.findChildrenOfAnyType(
            root,
            JustRecipeStatement::class.java,
            JustVariableStatement::class.java
        )
            .mapNotNull { statement ->
                if (statement is JustRecipeStatement) {
                    FoldingDescriptor(
                        statement, statement.textRange.startOffset, statement.textRange.endOffset - 1,
                        null,
                        statement.recipeName.text + ":"
                    )
                } else if (statement is JustVariableStatement) {
                    if (statement.text.trim().contains('\n')) {
                        FoldingDescriptor(
                            statement, statement.textRange.startOffset, statement.textRange.endOffset - 1,
                            null,
                            statement.variable.text + " := ..."
                        )
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
