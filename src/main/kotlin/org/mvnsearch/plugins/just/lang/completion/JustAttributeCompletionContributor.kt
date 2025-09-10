package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
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
                    if (element.elementType == JustTypes.ATTRIBUTE_NAME || element.elementType == JustTypes.CLOSE_BRACKET) {
                        result.addElement(LookupElementBuilder.create("linux"))
                        result.addElement(LookupElementBuilder.create("macos"))
                        result.addElement(LookupElementBuilder.create("unix"))
                        result.addElement(LookupElementBuilder.create("windows"))
                        result.addElement(LookupElementBuilder.create("openbsd"))
                        result.addElement(LookupElementBuilder.create("private"))
                        result.addElement(LookupElementBuilder.create("no-cd"))
                        result.addElement(LookupElementBuilder.create("working-directory"))
                        result.addElement(LookupElementBuilder.create("doc"))
                        result.addElement(LookupElementBuilder.create("group"))
                        result.addElement(LookupElementBuilder.create("exit-message"))
                        result.addElement(LookupElementBuilder.create("no-exit-message"))
                        result.addElement(LookupElementBuilder.create("confirm"))
                        result.addElement(LookupElementBuilder.create("no-quiet"))
                        result.addElement(LookupElementBuilder.create("no-exit-message"))
                        result.addElement(LookupElementBuilder.create("positional-arguments"))
                        result.addElement(LookupElementBuilder.create("script"))
                        result.addElement(LookupElementBuilder.create("extension"))
                        result.addElement(LookupElementBuilder.create("metadata"))
                        result.addElement(LookupElementBuilder.create("parallel"))
                        result.addElement(LookupElementBuilder.create("default"))
                    }
                }
            }
        )
    }
}