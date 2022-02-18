package org.mvnsearch.plugins.just.lang.psi

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.tree.IElementType
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.JustLanguage
import javax.swing.Icon

class JustTokenType(debugName: String) : IElementType(debugName, JustLanguage) {
    override fun toString(): String = "JustToken." + super.toString()
}

class JustElementType(debugName: String) : IElementType(debugName, JustLanguage)

object JustFileType : LanguageFileType(JustLanguage) {
    override fun getName(): String = "justfile"
    override fun getDescription(): String = "Just file"
    override fun getDefaultExtension(): String = "justfile"

    override fun getIcon(): Icon = JustIcons.JUST_FILE
}