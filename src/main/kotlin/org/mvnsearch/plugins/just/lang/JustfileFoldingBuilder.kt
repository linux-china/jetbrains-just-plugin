package org.mvnsearch.plugins.just.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.FoldingDescriptor.EMPTY_ARRAY
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
            return EMPTY_ARRAY
        }
        return PsiTreeUtil.findChildrenOfAnyType(
            root,
            JustRecipeStatement::class.java,
            JustVariableStatement::class.java
        )
            .mapNotNull { statement ->
                val placeholder = when (statement) {
                    is JustRecipeStatement -> statement.recipeName.text + ":"
                    is JustVariableStatement -> statement.variable.text + " := ..."
                    else -> return@mapNotNull null
                }
                val range = foldingRange(statement) ?: return@mapNotNull null
                FoldingDescriptor(statement.node, range, null, placeholder)
            }.toTypedArray()
    }

    /**
     * Range of the statement without trailing whitespace, only when it spans multiple lines
     */
    private fun foldingRange(statement: PsiElement): TextRange? {
        val text = statement.text
        val trimmed = text.trimEnd()
        if (!trimmed.contains('\n')) {
            return null
        }
        val start = statement.textRange.startOffset
        return TextRange(start, start + trimmed.length)
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return DEFAULT_PLACEHOLDER_TEXT
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
