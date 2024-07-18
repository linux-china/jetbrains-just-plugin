package org.mvnsearch.plugins.just.lang.insight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import org.mvnsearch.plugins.just.removeVariablePrefix


class JustfileAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            JustTypes.SHEBANG -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT).create()
            }

            JustTypes.RECIPE_NAME,
            JustTypes.DEPENDENCY_NAME,
            -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_METHOD).create()
            }

            JustTypes.ID -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
            }

            JustTypes.KEYWORD_MOD,
            JustTypes.KEYWORD_ALIAS,
            JustTypes.KEYWORD_SET,
            JustTypes.KEYWORD_EXPORT,
            JustTypes.KEYWORD_IMPORT -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.KEYWORD).create()
            }

            JustTypes.VARIABLE -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
            }

            JustTypes.ASSIGN -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.OPERATION_SIGN).create()
            }

            JustTypes.CODE -> {  // high light variable
                highLightVariablesInCodeBlock(element, holder)
            }

            JustTypes.COMMENT -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT).create()
            }

            JustTypes.ATTRIBUTE -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).textAttributes(DefaultLanguageHighlighterColors.METADATA).create()
            }

            JustTypes.RECIPE_PARAM_NAME -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
            }

            JustTypes.SETTING -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange).textAttributes(DefaultLanguageHighlighterColors.MARKUP_ENTITY).create()
            }

            JustTypes.STRING,  //text pair
            JustTypes.RAW_STRING,
            JustTypes.BACKTICK,
            JustTypes.INDENTED_BACKTICK,
            JustTypes.INDENTED_RAW_STRING,
            JustTypes.INDENTED_STRING -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.STRING).create()
            }
        }
    }

    private fun getFirstWord(line: String): String {
        var firstWord: String = line.split("[:=(]".toRegex(), 2)[0]
        if (line.contains(' ')) {
            firstWord = line.substring(0, line.indexOf(' '))
        }
        return removeVariablePrefix(firstWord)
    }


    private fun highLightVariablesInCodeBlock(element: PsiElement, holder: AnnotationHolder) {
        val rangeOffset = element.textRange.startOffset
        val text = element.text
        var offset = text.indexOf("{{")
        while (offset > 0) {
            val endOffset = text.indexOf("}}", offset + 2)
            if (endOffset > offset) {
                val range = TextRange(rangeOffset + offset, rangeOffset + endOffset + 2)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD).create()
                offset = text.indexOf("{{", endOffset + 2)
            } else {
                offset = -1
            }
        }
    }

}