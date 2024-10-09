package org.mvnsearch.plugins.just.lang.lighter

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.mvnsearch.plugins.just.lang.lexer.JustLexerAdapter
import org.mvnsearch.plugins.just.lang.psi.JustTypes.*

class JustSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return JustLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val textAttributesKey = when (tokenType) {
            SHEBANG -> DefaultLanguageHighlighterColors.LINE_COMMENT
            RECIPE_NAME, DEPENDENCY_NAME -> DefaultLanguageHighlighterColors.STATIC_METHOD
            EXPORT_NAME, RECIPE_PARAM_NAME -> DefaultLanguageHighlighterColors.STATIC_FIELD
            KEYWORD_MOD, KEYWORD_ALIAS, KEYWORD_SET, KEYWORD_EXPORT,
            KEYWORD_IF, KEYWORD_ELSE, KEYWORD_ELSE_IF, KEYWORD_EXPORT,
            KEYWORD_IMPORT -> DefaultLanguageHighlighterColors.KEYWORD

            VARIABLE -> DefaultLanguageHighlighterColors.STATIC_FIELD
            ASSIGN, PLUS, SLASH, EQEQ, NOEQ, REEQ -> DefaultLanguageHighlighterColors.OPERATION_SIGN
            COMMENT -> DefaultLanguageHighlighterColors.LINE_COMMENT
            ATTRIBUTE -> DefaultLanguageHighlighterColors.METADATA
            SETTING -> DefaultLanguageHighlighterColors.MARKUP_ENTITY
            FUNCTION_NAME -> DefaultLanguageHighlighterColors.FUNCTION_CALL
            X_INDICATOR -> DefaultLanguageHighlighterColors.KEYWORD
            STRING, RAW_STRING, BACKTICK,
            INDENTED_BACKTICK, INDENTED_RAW_STRING, INDENTED_STRING -> DefaultLanguageHighlighterColors.STRING

            else -> {
                null
            }
        }
        return textAttributesKey?.let { arrayOf(it) } ?: emptyArray()
    }
}