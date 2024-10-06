package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.run.JustConfigurationType.Companion.ID
import javax.swing.Icon
import kotlin.jvm.java

class JustConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String {
        return ID
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return JustRunConfigurationOptions::class.java
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return JustRunConfiguration(project, this, "Just")
    }

    override fun getIcon(): Icon {
        return JustIcons.JUST_FILE
    }
}