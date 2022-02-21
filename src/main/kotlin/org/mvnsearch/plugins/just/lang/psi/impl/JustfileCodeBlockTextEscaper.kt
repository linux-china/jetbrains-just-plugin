package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.mvnsearch.plugins.just.lang.psi.JustCodeBlock

class JustfileCodeBlockTextEscaper(private val codeBlock: JustCodeBlock) : LiteralTextEscaper<JustCodeBlock>(codeBlock) {
    override fun isOneLine(): Boolean {
        return codeBlock.text.lastIndexOf('\n') == 0
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        return rangeInsideHost.startOffset + offsetInDecoded
    }

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        return try {
            outChars.append(rangeInsideHost.substring(codeBlock.text))
            true
        } catch (e: Throwable) {
            false
        }
    }
}