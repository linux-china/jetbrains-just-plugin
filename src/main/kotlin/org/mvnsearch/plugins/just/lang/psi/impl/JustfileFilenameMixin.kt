package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import org.mvnsearch.plugins.just.Just

open class JustfileFilenameMixin internal constructor(astNode: ASTNode) : ASTWrapperPsiElement(astNode) {
    private class JustFileReferenceSet(text: String, element: PsiElement, offset: Int) : FileReferenceSet(text, element, offset, null, true) {
        override fun getReferenceCompletionFilter(): Condition<PsiFileSystemItem> {
            return Condition {
                it.isDirectory || Just.isJustfile(it.name)
            }
        }
    }

    override fun getReferences(): Array<out FileReference> {
        val text = node.text
        // skip `x`/`f` indicator and the opening quote, e.g. x'foo.just' or '''foo.just'''
        val indicatorLength = if (text.startsWith("x") || text.startsWith("f")) 1 else 0
        val quote = listOf("'''", "\"\"\"", "'", "\"").firstOrNull { text.startsWith(it, indicatorLength) }
            ?: return emptyArray()
        val start = indicatorLength + quote.length
        val end = if (text.length >= start + quote.length && text.endsWith(quote)) text.length - quote.length else text.length
        return JustFileReferenceSet(text.substring(start, end), this, start).allReferences
    }
}
