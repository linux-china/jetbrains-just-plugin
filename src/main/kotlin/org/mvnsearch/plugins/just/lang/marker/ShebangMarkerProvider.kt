package org.mvnsearch.plugins.just.lang.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.psi.JustSetStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class ShebangMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): @GutterName String {
        return "Shebang"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.elementType == JustTypes.CODE_BLOCK) {
            val elementText = psiElement.text
            val firstLine = elementText.trim().substringBefore('\n')
            if (firstLine.startsWith("#!")) {
                var icon = JustIcons.Bash
                var hint = "Shell"
                if (firstLine.contains("python") || firstLine.contains(" uv")) {
                    icon = JustIcons.Python
                    hint = "Python"
                } else if (firstLine.contains("ruby")) {
                    icon = JustIcons.Ruby
                    hint = "Ruby"
                } else if (firstLine.contains("bun")) {
                    icon = JustIcons.Bun
                    hint = "Bun"
                } else if (firstLine.contains("nu")) {
                    icon = JustIcons.Nushell
                    hint = "Nushell"
                }
                val offset = elementText.indexOf(firstLine)
                val elementStartOffset = psiElement.textRange.startOffset
                val textRange =
                    TextRange(elementStartOffset + offset + 1, elementStartOffset + offset + firstLine.length - 1)
                return LineMarkerInfo(
                    psiElement,
                    textRange,
                    icon,
                    { _: PsiElement? ->
                        hint
                    }, null,
                    GutterIconRenderer.Alignment.CENTER,
                    {
                        hint
                    }
                )
            }
        } else if (psiElement.elementType == JustTypes.SET_STATEMENT) {
            val setStatement = psiElement as JustSetStatement
            if (setStatement.setting.text == "shell" || setStatement.setting.text == "windows-shell") {
                setStatement.setStatementValue?.let { shellValueElement ->
                    var icon = JustIcons.Bash
                    var hint = "Shell"
                    val shellConfig = shellValueElement.text.trim()
                    if (shellConfig.contains("python") || shellConfig.contains(" uv")) {
                        icon = JustIcons.Python
                        hint = "Python"
                    } else if (shellConfig.contains("powershell") || shellConfig.contains("pwsh")) {
                        icon = JustIcons.PowerShell
                        hint = "PowerShell"
                    } else if (shellConfig.contains("bun")) {
                        icon = JustIcons.Bun
                        hint = "Bun"
                    } else if (shellConfig.contains("nu")) {
                        icon = JustIcons.Nushell
                        hint = "Nushell"
                    } else if (shellConfig.contains("duckdb")) {
                        icon = JustIcons.DuckDB
                        hint = "DuckDB"
                    }
                    return LineMarkerInfo(
                        psiElement,
                        setStatement.textRange,
                        icon,
                        { _: PsiElement? ->
                            hint
                        }, null,
                        GutterIconRenderer.Alignment.CENTER,
                        {
                            hint
                        }
                    )
                }
            }
        }
        return null
    }
}