package org.mvnsearch.plugins.just.ide.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader

object JustIcons {
    val JUST_FILE = IconLoader.findIcon("/icons/just-16x16.svg", JustIcons::class.java.classLoader)!!
    val JUST_FILE_12 = IconLoader.findIcon("/icons/just-12x12.svg", JustIcons::class.java.classLoader)!!
    val RUN_ICON = AllIcons.RunConfigurations.TestState.Run
    val VARIABLE_ICON = AllIcons.Nodes.Variable
    val SET_ICON = AllIcons.General.Settings
    val MOD_ICON = AllIcons.Nodes.Module
    var EXPORT_ICON = AllIcons.Nodes.Gvariable
    var IMPORT_ICON = AllIcons.ToolbarDecorator.Import
}