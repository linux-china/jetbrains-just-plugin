package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.lang.psi.JustSetStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes

class JustSetCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC, psiElement(PsiElement::class.java).withElementType(JustTypes.SETTING).withLanguage(JustLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    if (element.elementType == JustTypes.SETTING) {
                        result.addElement(LookupElementBuilder.create("allow-duplicate-recipes"))
                        result.addElement(LookupElementBuilder.create("dotenv-load := true").withPresentableText("dotenv-load"))
                        result.addElement(LookupElementBuilder.create("export"))
                        result.addElement(LookupElementBuilder.create("positional-arguments"))
                        result.addElement(LookupElementBuilder.create("shell := [\"bash\", \"-c\"]").withPresentableText("shell"))
                        result.addElement(LookupElementBuilder.create("windows-powershell"))
                    }
                }
            }
        )
    }
}