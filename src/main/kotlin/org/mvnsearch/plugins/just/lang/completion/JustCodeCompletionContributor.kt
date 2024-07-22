package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustRecipeParam
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import org.mvnsearch.plugins.just.removeVariablePrefix

/**
 * code completion for Code block, such as variables
 *
 * @author linux_china
 */
class JustCodeCompletionContributor : CompletionContributor() {
    companion object {
        val JUST_FUNCTIONS = listOf("home_dir()", "invocation_dir()", "justfile_dir()",
            "absolute_path()", "file_name()",
            "env()", "uuid()", "datetime()")
    }

    init {
        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(PsiElement::class.java).withElementType(JustTypes.CODE).withLanguage(JustLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    if (element.elementType == JustTypes.CODE) {

                        val caret = parameters.editor.caretModel.currentCaret
                        val lineOffset = caret.visualLineStart
                        val prefixText = parameters.editor.document.getText(TextRange(lineOffset, caret.offset))
                        if (prefixText.endsWith("$")) {
                            val prefixMatcher = result.withPrefixMatcher("$")
                            var suffix = ""
                            if (prefixText.length > 2) {
                                val character = prefixText[prefixText.length - 2]
                                if (character == '\'' || character == '"') {
                                    suffix = "$character"
                                }
                            }

                            val recipeSmt = element.parentOfType<JustRecipeStatement>()!!
                            extractRecipeVariables(recipeSmt).filter { it.startsWith("$") }.forEach {
                                val name = removeVariablePrefix(it)
                                prefixMatcher.addElement(LookupElementBuilder.create("$${name}${suffix}").withPresentableText(name).withIcon(AllIcons.Nodes.Variable))
                            }
                            val justfile = element.containingFile as JustFile
                            val justfileMetadata = justfile.parseMetadata(true)
                            justfileMetadata.envVariables.forEach {
                                prefixMatcher.addElement(LookupElementBuilder.create("$${it}${suffix}").withPresentableText(it).withIcon(AllIcons.Nodes.Variable))
                            }
                        } else if (prefixText.endsWith("{{")) {
                            val prefixMatcher = result.withPrefixMatcher("{{")
                            var suffix = ""
                            if (prefixText.length > 3) {
                                val character = prefixText[prefixText.length - 3]
                                if (character == '\'' || character == '"') {
                                    suffix = "$character"
                                }
                            }
                            val recipeSmt = element.parentOfType<JustRecipeStatement>()!!
                            extractRecipeVariables(recipeSmt).filter { !it.startsWith("$") }.forEach {
                                val name = removeVariablePrefix(it)
                                prefixMatcher.addElement(
                                    priority(
                                        LookupElementBuilder.create("{{$name}}${suffix}").withPresentableText(name).withIcon(AllIcons.Nodes.Variable),
                                        3.0
                                    )
                                )
                            }
                            val justfile = element.containingFile as JustFile
                            val justfileMetadata = justfile.parseMetadata(true)
                            justfileMetadata.variables.forEach {
                                prefixMatcher.addElement(priority(LookupElementBuilder.create("{{$it}}${suffix}").withPresentableText(it).withIcon(AllIcons.Nodes.Variable), 2.0))
                            }
                            JUST_FUNCTIONS.forEach {
                                prefixMatcher.addElement(priority(LookupElementBuilder.create("{{$it}}${suffix}").withPresentableText(it).withIcon(AllIcons.Nodes.Function), 0.0))
                            }
                        }
                    }
                }
            }
        )
    }

    fun extractRecipeVariables(recipeSmt: JustRecipeStatement): List<String> {
        val params = recipeSmt.params
        if (params != null) {
            return params.children.filterIsInstance<JustRecipeParam>().map {
                val text = it.text
                if (text.contains('=')) {
                    text.substring(0, text.indexOf('='))
                } else {
                    text
                }
            }.toList()
        }
        return emptyList()
    }

    fun priority(builder: LookupElementBuilder, priority: Double): LookupElement {
        return PrioritizedLookupElement.withPriority(builder, priority)
    }
}