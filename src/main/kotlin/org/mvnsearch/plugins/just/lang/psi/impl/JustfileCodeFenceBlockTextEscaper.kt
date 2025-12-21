package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock
import org.mvnsearch.plugins.just.lang.psi.JustCodeFenceBlock

class JustfileCodeFenceBlockTextEscaper(private val codeFenceBlock: JustCodeFenceBlock) : LiteralTextEscaper<JustCodeFenceBlock>(codeFenceBlock) {
    override fun isOneLine(): Boolean {
        return false
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        return rangeInsideHost.startOffset + offsetInDecoded
    }

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        return try {
            outChars.append(rangeInsideHost.substring(codeFenceBlock.text))
            true
        } catch (e: Throwable) {
            false
        }
    }
}