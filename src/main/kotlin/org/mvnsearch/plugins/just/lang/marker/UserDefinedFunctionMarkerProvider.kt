package org.mvnsearch.plugins.just.lang.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class UserDefinedFunctionMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): @GutterName String {
        return "UDF"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.elementType == JustTypes.FUNCTION_DECL_NAME) {
            val hint = psiElement.text
            return LineMarkerInfo(
                psiElement,
                psiElement.textRange,
                AllIcons.Nodes.Function,
                { _: PsiElement? ->
                    hint
                }, null,
                GutterIconRenderer.Alignment.CENTER,
                {
                    hint
                }
            )
        }
        return null
    }
}