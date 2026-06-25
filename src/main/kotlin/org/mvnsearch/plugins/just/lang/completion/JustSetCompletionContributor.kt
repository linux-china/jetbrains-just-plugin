package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
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
            CompletionType.BASIC,
            psiElement(PsiElement::class.java).withElementType(JustTypes.SETTING).withLanguage(JustLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    if (element.elementType == JustTypes.SETTING) {
                        val insertHandler: (InsertionContext, LookupElement) -> Unit =
                            { context, _ -> context.editor.caretModel.moveToOffset(context.tailOffset - 1) }
                        result.addElement(LookupElementBuilder.create("allow-duplicate-recipes"))
                        result.addElement(LookupElementBuilder.create("allow-duplicate-variables"))
                        result.addElement(
                            LookupElementBuilder.create("dotenv-load := true").withPresentableText("dotenv-load")
                        )
                        result.addElement(
                            LookupElementBuilder.create("dotenv-command := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(LookupElementBuilder.create("dotenv-required"))
                        result.addElement(
                            LookupElementBuilder.create("dotenv-filename := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(LookupElementBuilder.create("dotenv-override := true"))
                        result.addElement(
                            LookupElementBuilder.create("dotenv-path := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(LookupElementBuilder.create("ignore-comments"))
                        result.addElement(LookupElementBuilder.create("default-list := true"))
                        result.addElement(LookupElementBuilder.create("export"))
                        result.addElement(LookupElementBuilder.create("guards"))
                        result.addElement(LookupElementBuilder.create("lists"))
                        result.addElement(LookupElementBuilder.create("quiet"))
                        result.addElement(LookupElementBuilder.create("unstable"))
                        result.addElement(LookupElementBuilder.create("fallback"))
                        result.addElement(LookupElementBuilder.create("script-interpreter"))
                        result.addElement(LookupElementBuilder.create("default-script := true"))
                        result.addElement(LookupElementBuilder.create("no-cd"))
                        result.addElement(
                            LookupElementBuilder.create("tempdir := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(
                            LookupElementBuilder.create("working-directory := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(
                            LookupElementBuilder.create("minimum-version := \"\"").withInsertHandler(insertHandler)
                        )
                        result.addElement(LookupElementBuilder.create("positional-arguments"))
                        result.addElement(
                            LookupElementBuilder.create("shell := [\"bash\", \"-c\"]").withPresentableText("shell")
                        )
                        result.addElement(
                            LookupElementBuilder.create("windows-shell := [\"pwsh.exe\", \"-NoLogo\",\"-Command\"]")
                                .withPresentableText("windows-shell")
                        )
                        result.addElement(LookupElementBuilder.create("windows-powershell := true"))
                    }
                }
            }
        )
    }
}
