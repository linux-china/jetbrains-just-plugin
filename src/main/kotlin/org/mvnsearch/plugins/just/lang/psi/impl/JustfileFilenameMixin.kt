package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*

open class JustfileFilenameMixin internal constructor(astNode: ASTNode) : ASTWrapperPsiElement(astNode) {
    override fun getReferences(): Array<out FileReference> {
        val unquoted = StringUtil.unquoteString(node.psi.text)
        return FileReferenceSet(unquoted, node.psi, 1, null, true).allReferences
    }
}
