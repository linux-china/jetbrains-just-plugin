package org.mvnsearch.plugins.just.lang.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class CommandLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): @GutterName String {
        return "Command mode"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.elementType == JustTypes.CODE_BLOCK) {
            val elementText = psiElement.text
            val lines = elementText.trim().lines()
            val firstLine = lines.first()
            if (firstLine.trim().startsWith("#!")) {
                val offset = elementText.indexOf(firstLine)
                val elementStartOffset = psiElement.startOffset
                val textRange =
                    TextRange(elementStartOffset + offset + 1, elementStartOffset + offset + firstLine.length - 1)
                return LineMarkerInfo(
                    psiElement,
                    textRange,
                    com.intellij.icons.AllIcons.Run.ShowIgnored,
                    { _: PsiElement? ->
                        "Quiet mode"
                    }, null,
                    GutterIconRenderer.Alignment.CENTER,
                    {
                        "Quiet mode"
                    }
                )
            }
        }
        return null
    }
}