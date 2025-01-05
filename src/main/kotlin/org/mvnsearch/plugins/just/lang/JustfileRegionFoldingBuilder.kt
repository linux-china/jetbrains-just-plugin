package org.mvnsearch.plugins.just.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.FoldingDescriptor.EMPTY
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustTypes

class JustfileRegionFoldingBuilder : FoldingBuilderEx(), DumbAware {
    companion object {
        const val DEFAULT_PLACEHOLDER_TEXT = "..."
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is JustFile) {
            return EMPTY
        }
        val regionElements = root.children
            .filter { it.elementType == JustTypes.COMMENT }
            .filter { it.text.startsWith("# region") || it.text.startsWith("# endregion") }
            .toList()
        val descriptors = mutableListOf<FoldingDescriptor>()
        // for loop with index
        for (i in regionElements.indices step 2) {
            val startElement = regionElements[i]
            val endElement = regionElements[i + 1]
            val start = startElement.textRange.startOffset
            val end = endElement.textRange.endOffset
            val foldingDescriptor = FoldingDescriptor(startElement, start, end, null, startElement.text.substring(2))
            descriptors.add(foldingDescriptor)
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return DEFAULT_PLACEHOLDER_TEXT
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
