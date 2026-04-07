package org.mvnsearch.plugins.just.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.sh.ShLanguage
import com.intellij.sh.ShTypes
import com.intellij.util.ProcessingContext
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustRecipeParam
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement

/**
 * code completion for sh string
 *
 * @author linux_china
 */
class JustVariableFunctionCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiElement::class.java)
                .withElementType(ShTypes.RAW_STRING)
                .withLanguage(ShLanguage.INSTANCE)
                .with(object : PatternCondition<PsiElement>("isInjectedInJustfile") {
                    override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
                        val manager = InjectedLanguageManager.getInstance(element.project)
                        val host = manager.getInjectionHost(element)
                        return host?.containingFile is JustFile
                    }
                })
                .withText(object : ElementPattern<String> {
                    override fun accepts(o: Any?): Boolean {
                        return o is String && o.contains("'{{")
                    }

                    override fun accepts(
                        o: Any?,
                        context: ProcessingContext?
                    ): Boolean {
                        return o is String && o.contains("'{{")
                    }

                    override fun getCondition(): ElementPatternCondition<String>? {
                        return null
                    }
                }),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    val caret = parameters.editor.caretModel.currentCaret
                    val lineOffset = caret.visualLineStart
                    val lineEndOffset = caret.visualLineEnd
                    val prefixText = parameters.editor.document.getText(TextRange(lineOffset, caret.offset))
                    val prefix = prefixText.substring(prefixText.lastIndexOf("{{"))
                    val prefixMatcher = result.withPrefixMatcher(prefix)
                    var suffix = if (prefix.endsWith(" ")) {
                        " }}"
                    } else {
                        "}}"
                    }
                    val suffixText = parameters.editor.document.getText(TextRange(caret.offset, lineEndOffset))
                    if (suffixText.contains("}}")) {
                        suffix = if (prefix.endsWith(" ") && !suffixText.startsWith(" ")) {
                            " "
                        } else {
                            ""
                        }
                    }
                    val manager = InjectedLanguageManager.getInstance(element.project)
                    val host = manager.getInjectionHost(element)!!
                    val justfile = host.containingFile as JustFile
                    val justfileMetadata = justfile.parseMetadata(true)
                    justfileMetadata.variables.forEach {
                        prefixMatcher.addElement(
                            priority(
                                LookupElementBuilder.create("{{$it}}${suffix}").withPresentableText(it)
                                    .withIcon(AllIcons.Nodes.Variable), 2.0
                            )
                        )
                    }
                    justfileMetadata.userDefinedFunctions.forEach {
                        prefixMatcher.addElement(
                            priority(
                                LookupElementBuilder.create("${prefix}$it()${suffix}")
                                    .withPresentableText("$it()")
                                    .withIcon(AllIcons.Nodes.Function), 0.0
                            )
                        )
                    }
                    JUST_FUNCTIONS.forEach {
                        prefixMatcher.addElement(
                            priority(
                                LookupElementBuilder.create("${prefix}$it${suffix}").withPresentableText(it)
                                    .withIcon(AllIcons.Nodes.Function), 0.0
                            )
                        )
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