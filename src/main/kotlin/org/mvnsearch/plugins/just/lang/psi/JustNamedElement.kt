package org.mvnsearch.plugins.just.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException

interface JustNamedElement : PsiNameIdentifierOwner {
}


abstract class JustNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JustNamedElement {
    private var _name: String? = null
    
    override fun getName(): String? {
        return this._name
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: @NlsSafe String): PsiElement? {
        this._name = name;
        return this
    }
}