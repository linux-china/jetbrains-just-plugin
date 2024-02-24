package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*

open class JustfileFilenameMixin internal constructor(astNode: ASTNode) : ASTWrapperPsiElement(astNode) {
    private class JustFileReferenceSet(text: String, element: PsiElement, offset: Int = 1) : FileReferenceSet(text, element, offset, null, true) {
        override fun getReferenceCompletionFilter(): Condition<PsiFileSystemItem> {
            return Condition {
                it.isDirectory
                || it.virtualFile.name.endsWith(".just")
                || listOf("justfile", "Justfile", ".justfile").contains(it.name)
            }
        }
    }

    override fun getReferences(): Array<out FileReference> =
        JustFileReferenceSet(StringUtil.unquoteString(node.psi.text), node.psi).allReferences
}
