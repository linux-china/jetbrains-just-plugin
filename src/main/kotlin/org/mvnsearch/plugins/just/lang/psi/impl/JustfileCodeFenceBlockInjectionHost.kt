package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock
import org.mvnsearch.plugins.just.lang.psi.JustCodeFenceBlock
import org.mvnsearch.plugins.just.lang.psi.JustfileElementFactory

abstract class JustfileCodeFenceBlockInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), JustCodeFenceBlock {

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return JustfileCodeFenceBlockTextEscaper(this)
    }

    override fun updateText(text: String): JustCodeFenceBlock {
        val codeBlock = JustfileElementFactory.createCodeFenceBlock(project, text)
        return this.replace(codeBlock) as JustCodeFenceBlock
    }

    override fun isValidHost(): Boolean {
        return true
    }
}