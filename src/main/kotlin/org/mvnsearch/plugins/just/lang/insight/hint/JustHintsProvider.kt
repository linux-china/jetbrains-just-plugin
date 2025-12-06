package org.mvnsearch.plugins.just.lang.insight.hint

import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes


@Suppress("UnstableApiUsage")
class JustHintsProvider : InlayParameterHintsProvider {
    override fun getParameterHints(element: PsiElement): List<InlayInfo?> {
        val elementType = element.elementType
        if (elementType == JustTypes.RECIPE_PARAM_NAME) {
            val paramName = element.text
            if (paramName.startsWith("$")) {
                return listOf(InlayInfo("exported", element.textRange.endOffset))
            }
        } else if (elementType == JustTypes.BACKTICK || elementType == JustTypes.INDENTED_BACKTICK) {
            return listOf(InlayInfo("shell command", element.textRange.startOffset))
        }
        return super.getParameterHints(element)
    }

    override fun getDefaultBlackList(): Set<String?> {
        return emptySet<String?>()
    }
}