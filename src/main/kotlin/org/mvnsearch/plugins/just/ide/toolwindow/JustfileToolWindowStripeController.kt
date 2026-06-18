package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface JustfileToolWindowStripeController {
    companion object {
        val EP_NAME =
            ExtensionPointName<JustfileToolWindowStripeController>("org.mvnsearch.just.toolWindowStripeController")
    }

    fun shouldHideStripeIconFor(project: Project): Boolean
}