package org.mvnsearch.plugins.just.lang.run

import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel


class JustRunSettingsEditor : SettingsEditor<JustRunConfiguration>() {
    private val myPanel: JPanel = JPanel()
    private var myJustFileName: LabeledComponent<TextFieldWithBrowseButton> = LabeledComponent()
    private var myRecipeName: LabeledComponent<JBTextField> = LabeledComponent()
    private var myRecipeArgs: LabeledComponent<ExtendableTextField> = LabeledComponent()
    private var myEnvVariables: LabeledComponent<JBTextArea> = LabeledComponent()

    init {
        myPanel.layout = BoxLayout(myPanel, BoxLayout.Y_AXIS)
        myJustFileName.component = TextFieldWithBrowseButton()
        myRecipeName.component = JBTextField()
        myRecipeArgs.component = ExtendableTextField()
        MacrosDialog.addMacroSupport(myRecipeArgs.component, { true }, { true })
        myEnvVariables.component = JBTextArea(3, 0)
        myJustFileName.label.text = "Justfile name:"
        myRecipeName.label.text = "Recipe name:"
        myRecipeArgs.label.text = "Recipe args:"
        myEnvVariables.label.text = "Env variables(.env style):"
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