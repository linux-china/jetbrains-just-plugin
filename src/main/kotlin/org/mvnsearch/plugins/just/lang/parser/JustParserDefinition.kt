package org.mvnsearch.plugins.just.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.lang.lexer.JustLexerAdapter
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class JustParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = JustLexerAdapter()

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = LITERALS

    override fun createParser(project: Project?): PsiParser = JustParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = JustFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements =
        ParserDefinition.SpaceRequirements.MAY

    override fun createElement(node: ASTNode?): PsiElement = JustTypes.Factory.createElement(node)

    companion object {
        val WHITE_SPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS: TokenSet = TokenSet.create(JustTypes.COMMENT)
        val LITERALS: TokenSet = TokenSet.create(JustTypes.STRING, JustTypes.RAW_STRING, JustTypes.BACKTICK,
            JustTypes.INDENTED_STRING, JustTypes.INDENTED_BACKTICK, JustTypes.INDENTED_RAW_STRING)
        val FILE: IFileElementType = IFileElementType(JustLanguage)
    }
}