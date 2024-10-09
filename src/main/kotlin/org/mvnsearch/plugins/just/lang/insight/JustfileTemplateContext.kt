package org.mvnsearch.plugins.just.lang.insight

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import org.mvnsearch.plugins.just.Just

class JustfileTemplateContext : TemplateContextType("JUSTFILE") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val fileName = templateActionContext.file.name
        return Just.isJustfile(fileName)
    }
}