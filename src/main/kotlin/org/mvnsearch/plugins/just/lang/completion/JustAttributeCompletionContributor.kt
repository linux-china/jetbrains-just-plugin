package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
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
                        // boolean / target attributes without arguments
                        for (name in PLAIN_ATTRIBUTES) {
                            result.addElement(LookupElementBuilder.create(name))
                        }
                        // attributes taking a single quoted-string argument: insert `name("")` with caret inside the quotes
                        for (name in SINGLE_STRING_ATTRIBUTES) {
                            result.addElement(
                                LookupElementBuilder.create(name)
                                    .withTailText("(\"\")", true)
                                    .withInsertHandler(SINGLE_STRING_INSERT_HANDLER)
                            )
                        }
                        // [env("NAME", "VALUE")]: insert with caret inside the first string
                        result.addElement(
                            LookupElementBuilder.create("env")
                                .withTailText("(\"\", \"\")", true)
                                .withInsertHandler(ENV_INSERT_HANDLER)
                        )
                    }
                }
            }
        )
    }

    companion object {
        private val PLAIN_ATTRIBUTES = listOf(
            "linux", "macos", "unix", "windows", "openbsd", "dragonfly", "netbsd", "freebsd", "android",
            "private", "no-cd", "no-exit-message", "exit-message", "no-quiet", "positional-arguments",
            "parallel", "default", "shell"
        )

        private val SINGLE_STRING_ATTRIBUTES = listOf(
            "confirm", "group", "doc", "working-directory", "extension", "script", "metadata", "arg"
        )

        /** Inserts `("")` after the attribute name and puts the caret between the quotes: `[doc("<caret>")]`. */
        private val SINGLE_STRING_INSERT_HANDLER = InsertHandler<LookupElement> { context, _ ->
            val offset = context.tailOffset
            context.document.insertString(offset, "(\"\")")
            context.editor.caretModel.moveToOffset(offset + 2)
        }

        /** Inserts `("", "")` after `env` and puts the caret inside the first string: `[env("<caret>", "")]`. */
        private val ENV_INSERT_HANDLER = InsertHandler<LookupElement> { context, _ ->
            val offset = context.tailOffset
            context.document.insertString(offset, "(\"\", \"\")")
            context.editor.caretModel.moveToOffset(offset + 2)
        }
    }
}
