package org.mvnsearch.plugins.just.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.tree.IElementType
import org.mvnsearch.plugins.just.ide.icons.JustIcons.JUST_FILE_12
import org.mvnsearch.plugins.just.lang.JustLanguage
import javax.swing.Icon

class JustTokenType(debugName: String) : IElementType(debugName, JustLanguage) {
    override fun toString(): String = "JustToken." + super.toString()
}

class JustElementType(debugName: String) : IElementType(debugName, JustLanguage)

object JustFileType : LanguageFileType(JustLanguage) {
    override fun getName(): String = "justfile"
    override fun getDescription(): String = "Just file"
    override fun getDefaultExtension(): String = "just"

    override fun getIcon(): Icon = JUST_FILE_12
}