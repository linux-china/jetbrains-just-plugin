package org.mvnsearch.plugins.just.lang.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import javax.swing.Icon


class AttributeMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): @GutterName String {
        return "Attribute"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.elementType == JustTypes.ATTRIBUTE_NAME) {
            val attributeName = psiElement.text
            val icon: Icon? = when (attributeName) {
                "windows" -> JustIcons.Windows
                "linux" -> JustIcons.Linux
                "macos" -> JustIcons.MacOS
                "android" -> JustIcons.Android
                "unix" -> JustIcons.Unix
                else -> null
            }
            if (icon != null) {
                val hint = "Run on $attributeName"
                return LineMarkerInfo(
                    psiElement,
                    psiElement.textRange,
                    icon,
                    { _: PsiElement? ->
                        hint
                    }, null,
                    GutterIconRenderer.Alignment.CENTER,
                    {
                        hint
                    }
                )
            } else if (attributeName == "arg") {
                return LineMarkerInfo(
                    psiElement,
                    psiElement.textRange,
                    AllIcons.Nodes.Variable,
                    { _: PsiElement? ->
                        "Argument"
                    }, null,
                    GutterIconRenderer.Alignment.CENTER,
                    {
                        "Argument"
                    }
                )
            }
        }
        return null
    }
}