package org.jetbrains.plugins.template

import com.intellij.lang.Language
import com.intellij.lang.injection.general.Injection
import com.intellij.lang.injection.general.LanguageInjectionContributor
import com.intellij.lang.injection.general.SimpleInjection
import com.intellij.psi.PsiElement
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock


class JustCodeBlockLanguageInjector : LanguageInjectionContributor {
    var shellLanguage: Language? = null

    init {
        shellLanguage = Language.findLanguageByID("Shell Script");
        if (shellLanguage == null) {
            shellLanguage = Language.findLanguageByID("BashPro Shell Script")
        }
    }

    override fun getInjection(context: PsiElement): Injection? {
        if (context is JustCodeBlock && shellLanguage != null) {
            return SimpleInjection(shellLanguage!!, "", "", null)
        }
        return null
    }


}