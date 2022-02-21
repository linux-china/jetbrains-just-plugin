package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock
import org.mvnsearch.plugins.just.lang.psi.JustfileElementFactory

abstract class JustfileCodeBlockInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), JustCodeBlock {

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return JustfileCodeBlockTextEscaper(this)
    }

    override fun updateText(text: String): JustCodeBlock {
        val codeBlock = JustfileElementFactory.createCodeBlock(project, text)
        return this.replace(codeBlock) as JustCodeBlock
    }

    override fun isValidHost(): Boolean {
        return true
    }
}