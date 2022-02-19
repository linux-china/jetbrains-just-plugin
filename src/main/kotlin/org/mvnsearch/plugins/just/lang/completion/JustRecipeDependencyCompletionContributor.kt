package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import org.mvnsearch.plugins.just.parseRecipeName

class JustRecipeDependencyCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement(PsiElement::class.java).withParent(psiElement(JustTypes.DEPENDENCY)).withElementType(JustTypes.DEPENDENCY_NAME).withLanguage(JustLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    val caret = parameters.editor.caretModel.currentCaret
                    val lineOffset = caret.visualLineStart
                    val prefixText = parameters.editor.document.getText(TextRange(lineOffset, caret.offset))
                    if (prefixText.contains(':')) {  //recipe names completion
                        val excludedNames = mutableListOf(parseRecipeName(prefixText))
                        val line = parameters.editor.document.getText(TextRange(lineOffset, caret.visualLineEnd))
                        val dependencies = line.substring(line.indexOf(':') + 1).trim()
                        if (dependencies.isNotEmpty()) {
                            excludedNames.addAll(dependencies.split("\\s+".toRegex()))
                        }
                        val justfile = element.containingFile as JustFile
                        val justfileMetadata = justfile.parseMetadata(false)
                        justfileMetadata.recipes.filter { !excludedNames.contains(it) }.forEach {
                            val buildItem = if (prefixText.endsWith("(")) {
                                LookupElementBuilder.create("$it \"\")").withPresentableText(it)
                                    .withInsertHandler { context, item ->
                                        run {
                                            context.editor.caretModel.moveToOffset(caret.offset - 2)
                                        }
                                    }
                            } else {
                                LookupElementBuilder.create(it)
                            }
                            result.addElement(buildItem)
                        }
                    }
                }
            }
        )
    }
}