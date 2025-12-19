package org.mvnsearch.plugins.just.lang.injector

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.mvnsearch.plugins.just.lang.psi.JustCodeFenceBlock


class JustCodeFenceBlockLanguageInjector : MultiHostInjector {
    private var shellLanguage: Language? = null
    private val languageMap = mutableMapOf<String, Language>()

    init {
        shellLanguage = Language.findLanguageByID("Shell Script")
        for (language in Language.getRegisteredLanguages()) {
            languageMap[language.id.lowercase()] = language
        }
    }

    fun getLanguage(language: String): Language? {
        return when (language) {
            "shell", "bash", "zsh", "sh", "" -> shellLanguage
            else -> languageMap[language]
        }
    }


    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val text = context.text
        val offset = text.indexOf("```")
        val offsetLineBreak = text.indexOf('\n', offset + 3)
        val offsetEnd = text.indexOf("```", offsetLineBreak + 1)
        var languageId = ""
        if (offsetLineBreak > 0 && offsetLineBreak > offset) {
            languageId = text.substring(offset + 3, offsetLineBreak).trim()
        }
        val language = getLanguage(languageId.lowercase())
        if (offsetEnd > offset && language != null) {
            val injectionTextRange = TextRange(
                offsetLineBreak + 1,
                offsetEnd
            )
            registrar.startInjecting(language)
            // add prefix to declare variables
            registrar.addPlace(
                languageId,
                null,
                context as PsiLanguageInjectionHost,
                injectionTextRange
            )
            registrar.doneInjecting()
        }
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(JustCodeFenceBlock::class.java)
    }

}