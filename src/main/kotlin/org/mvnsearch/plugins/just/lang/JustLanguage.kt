package org.mvnsearch.plugins.just.lang

import com.intellij.lang.Language

object JustLanguage : Language("Just") {
    private fun readResolve(): Any = JustLanguage
    override fun isCaseSensitive() = true
}