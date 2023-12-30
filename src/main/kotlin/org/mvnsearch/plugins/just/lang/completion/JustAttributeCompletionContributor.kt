package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.lang.psi.JustTypes

class JustAttributeCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC, psiElement(PsiElement::class.java).withLanguage(JustLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition
                    if (element.elementType == JustTypes.ATTRIBUTE) {
                        result.addElement(LookupElementBuilder.create("linux"))
                        result.addElement(LookupElementBuilder.create("macos"))
                        result.addElement(LookupElementBuilder.create("unix"))
                        result.addElement(LookupElementBuilder.create("windows"))
                        result.addElement(LookupElementBuilder.create("private"))
                        result.addElement(LookupElementBuilder.create("no-cd"))
                        result.addElement(LookupElementBuilder.create("no-exit-message"))
                        result.addElement(LookupElementBuilder.create("confirm"))
                    }
                }
            }
        )
    }
}