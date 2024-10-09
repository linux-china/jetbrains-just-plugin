package org.mvnsearch.plugins.just.lang.insight

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.mvnsearch.plugins.just.lang.psi.JustTypes


class JustBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(JustTypes.OPEN_PAREN, JustTypes.CLOSE_PAREN, true),
        BracePair(JustTypes.OPEN_BRACKET, JustTypes.CLOSE_BRACKET, true)
    )

    override fun getPairs(): Array<BracePair> {
        return pairs
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}