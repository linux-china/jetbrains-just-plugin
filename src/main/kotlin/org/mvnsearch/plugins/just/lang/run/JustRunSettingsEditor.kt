package org.mvnsearch.plugins.just.lang.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


class JustRunSettingsEditor : SettingsEditor<JustRunConfiguration>() {
    private val myPanel: JPanel = JPanel()
    private var myJustFileName: LabeledComponent<TextFieldWithBrowseButton> = LabeledComponent()
    private var myRecipeName: LabeledComponent<JTextField> = LabeledComponent()
    private var myRecipeArgs: LabeledComponent<JTextField> = LabeledComponent()
    private var myEnvVariables: LabeledComponent<JTextField> = LabeledComponent()

    init {
        myPanel.layout = BoxLayout(myPanel, BoxLayout.Y_AXIS)
        myJustFileName.component = TextFieldWithBrowseButton()
        myRecipeName.component = JTextField()
        myRecipeArgs.component = JTextField()
        myEnvVariables.component = JTextField()
        myJustFileName.label.text = "Justfile name"
        myRecipeName.label.text = "Recipe name"
        myRecipeArgs.label.text = "Recipe args"
        myEnvVariables.label.text = "Env variables"
        myPanel.add(myJustFileName)
        myPanel.add(myRecipeName)
        myPanel.add(myRecipeArgs)
        myPanel.add(myEnvVariables)
    }

    override fun resetEditorFrom(justRunConfiguration: JustRunConfiguration) {
        myJustFileName.component.text = justRunConfiguration.getFileName() ?: ""
        myRecipeName.component.text = justRunConfiguration.getRecipeName() ?: ""
        myRecipeArgs.component.text = justRunConfiguration.getRecipeArgs() ?: ""
        myEnvVariables.component.text = justRunConfiguration.getEnvVariables() ?: ""
    }

    override fun applyEditorTo(justRunConfiguration: JustRunConfiguration) {
        justRunConfiguration.setFileName(myJustFileName.component.text)
        justRunConfiguration.setRecipeName(myRecipeName.component.text)
        justRunConfiguration.setRecipeArgs(myRecipeArgs.component.text)
        justRunConfiguration.setEnvVariables(myEnvVariables.component.text)
    }

    override fun createEditor(): JComponent {
        return myPanel
    }


}