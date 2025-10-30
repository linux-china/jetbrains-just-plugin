package org.mvnsearch.plugins.just.lang.injector

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.parentOfType
import org.mvnsearch.plugins.just.INDENT_CHARS
import org.mvnsearch.plugins.just.PARAM_PREFIX_LIST
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement


class JustCodeBlockLanguageInjector : MultiHostInjector {
    private var shellLanguage: Language? = null
    private var sqlLanguage: Language? = null

    init {
        sqlLanguage = Language.findLanguageByID("SQL")
        shellLanguage = Language.findLanguageByID("Shell Script")
        if (shellLanguage == null) {
            shellLanguage = Language.findLanguageByID("BashPro Shell Script")
        }
    }


    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (shellLanguage != null) {
            val justFile = context.containingFile as JustFile
            if (justFile.isBashAlike()) { // validate global shell setting
                val text = context.text
                if (isShellCode(text.trim())) {
                    var injectionScript = justFile.getExportedVariables().joinToString(separator = "") { "$it=''\n" }
                    val recipeStatement = context.parentOfType<JustRecipeStatement>()
                    if (recipeStatement != null) {
                        recipeStatement.params?.recipeParamList?.forEach {
                            it.recipeParamName.text?.let { name ->
                                if (name.startsWith("$")) {
                                    val shellVariableName = name.substring(1)
                                    injectionScript += "$shellVariableName=''\n"
                                }
                            }
                        }
                    }
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
                            // add prefix to declare variables
                            registrar.addPlace(
                                injectionScript,
                                null,
                                context as PsiLanguageInjectionHost,
                                injectionTextRange
                            )
                            registrar.doneInjecting()
                        }
                    }
                }
            } else if (sqlLanguage != null && justFile.isSQLAlike()) {
                val text = context.text
                val offset = text.indexOfFirst { !INDENT_CHARS.contains(it) }
                if (offset > 0) {
                    val injectionTextRange = TextRange(offset, context.textLength)
                    registrar.startInjecting(sqlLanguage!!)
                    registrar.addPlace(
                        null,
                        null,
                        context as PsiLanguageInjectionHost,
                        injectionTextRange
                    )
                    registrar.doneInjecting()
                }
            }
        }
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(JustCodeBlock::class.java)
    }

    private fun isShellCode(code: String): Boolean {
        if (code.contains("{{") || code.contains("}}")) {
            // enable highlight for parameter in string
            return (code.contains("\"{{") || code.contains("'{{")) && !code.contains(" {{")
        }
        // check shell shebang
        return !code.startsWith("#!")
                || code.startsWith("#!/usr/bin/env sh")
                || code.startsWith("#!/usr/bin/env bash")
                || code.startsWith("#!/usr/bin/env zsh")
                || code.startsWith("#!/usr/bin/env fish")
    }

}