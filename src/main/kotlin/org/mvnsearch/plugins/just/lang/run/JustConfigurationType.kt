package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import javax.swing.Icon

class JustConfigurationType : ConfigurationType {
    companion object {
        const val ID = "JustRunConfiguration"
    }

    override fun getDisplayName(): String {
        return "Just"
    }

    override fun getConfigurationTypeDescription(): String {
        return "Just run configuration type"
    }

    override fun getIcon(): Icon {
        return JustIcons.JUST_FILE
    }

    override fun getId(): String {
        return ID
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(JustConfigurationFactory(this))
    }


}