package org.mvnsearch.plugins.just.lang.insight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class JustfileAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {

            JustTypes.CONDITIONAL_BLOCK -> {  // high light variable
                highLightStringInConditionalBlock(element, holder)
            }

            JustTypes.CODE -> {  // high light variable
                highLightVariablesInCodeBlock(element, holder)
            }

            JustTypes.F_STRING -> {  // high light variable
                highLightVariablesInFormatString(element, holder)
            }

            JustTypes.X_STRING, JustTypes.X_RAW_STRING,
            JustTypes.X_INDENTED_STRING, JustTypes.X_INDENTED_RAW_STRING -> {  // high light variable
                highLightEnvVariablesInXString(element, holder)
            }
        }
    }

    private fun highLightVariablesInCodeBlock(element: PsiElement, holder: AnnotationHolder) {
        val rangeOffset = element.textRange.startOffset
        val text = element.text
        var offset = text.indexOf("{{")
        while (offset > 0) {
            val endOffset = text.indexOf("}}", offset + 2)
            if (endOffset > offset) {
                val range = TextRange(rangeOffset + offset, rangeOffset + endOffset + 2)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
                offset = text.indexOf("{{", endOffset + 2)
            } else {
                offset = -1
            }
        }
    }

    private fun highLightVariablesInFormatString(element: PsiElement, holder: AnnotationHolder) {
        val rangeOffset = element.textRange.startOffset
        val text = element.text
        var offset = text.indexOf("{{")
        while (offset > 0) {
            val endOffset = text.indexOf("}}", offset + 2)
            if (endOffset > offset) {
                val range = TextRange(rangeOffset + offset, rangeOffset + endOffset + 2)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
                offset = text.indexOf("{{", endOffset + 2)
            } else {
                offset = -1
            }
        }
    }

    private fun highLightStringInConditionalBlock(element: PsiElement, holder: AnnotationHolder) {
        val rangeOffset = element.textRange.startOffset
        val text = element.text
        val chars = arrayOf('\'', '"')
        for (char in chars) {
            var offset = text.indexOf(char)
            while (offset > 0) {
                val endOffset = text.indexOf(char, offset + 1)
                if (endOffset > offset) {
                    val range = TextRange(rangeOffset + offset, rangeOffset + endOffset + 1)
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.STRING).create()
                    offset = text.indexOf(char, endOffset + 1)
                } else {
                    offset = -1
                }
            }
        }
    }

    private fun highLightEnvVariablesInXString(element: PsiElement, holder: AnnotationHolder) {
        val rangeOffset = element.textRange.startOffset
        val text = element.text
        var offset = text.indexOf("\${")
        while (offset > 0) {
            val endOffset = text.indexOf("}", offset + 2)
            if (endOffset > offset) {
                val range = TextRange(rangeOffset + offset, rangeOffset + endOffset + 1)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
                offset = text.indexOf("\${", endOffset + 2)
            } else {
                offset = -1
            }
        }
    }

}