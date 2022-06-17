package org.mvnsearch.plugins.just.lang.injector

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.mvnsearch.plugins.just.INDENT_CHARS
import org.mvnsearch.plugins.just.PARAM_PREFIX_LIST
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock


class JustCodeBlockLanguageInjector : MultiHostInjector, DumbAware {
    var shellLanguage: Language? = null

    init {
        shellLanguage = Language.findLanguageByID("Shell Script");
        if (shellLanguage == null) {
            shellLanguage = Language.findLanguageByID("BashPro Shell Script")
        }
    }


    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val text = context.text
        if (!text.contains("#!/usr/bin/env") || text.contains("#!/usr/bin/env sh")) {
            val offset = text.indexOfFirst { !INDENT_CHARS.contains(it) && !PARAM_PREFIX_LIST.contains(it) }
            if (offset > 0) {
                var trailLength = text.toCharArray().reversedArray().indexOfFirst { !INDENT_CHARS.contains(it) }
                if (trailLength < 0) {
                    trailLength = 0
                }
                val endOffset = context.textLength - trailLength
                if (endOffset > offset) {
                    val injectionTextRange = TextRange(offset, context.textLength - trailLength)
                    registrar.startInjecting(shellLanguage!!)
                    registrar.addPlace(null, null, context as PsiLanguageInjectionHost, injectionTextRange)
                    registrar.doneInjecting()
                }
            }
        }
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(JustCodeBlock::class.java)
    }

}