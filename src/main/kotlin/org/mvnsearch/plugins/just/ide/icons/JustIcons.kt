package org.mvnsearch.plugins.just.ide.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object JustIcons {
    val JUST_FILE = IconLoader.findIcon("/icons/just-16x16.svg", JustIcons::class.java.classLoader)!!
    val JUST_FILE_12 = IconLoader.findIcon("/icons/just-12x12.svg", JustIcons::class.java.classLoader)!!
    val RUN_ICON = AllIcons.RunConfigurations.TestState.Run
    val DEFAULT_RUN_ICON = AllIcons.RunConfigurations.TestState.Run_run
    val VARIABLE_ICON = AllIcons.Nodes.Variable
    val SET_ICON = AllIcons.General.Settings
    val MOD_ICON = AllIcons.Nodes.Module
    var EXPORT_ICON = AllIcons.Nodes.Gvariable
    var IMPORT_ICON = AllIcons.ToolbarDecorator.Import
    var Bash: Icon = IconLoader.findIcon("/icons/bash.png", JustIcons::class.java.classLoader)!!
    var Nushell: Icon = IconLoader.findIcon("/icons/nushell.png", JustIcons::class.java.classLoader)!!
    var PowerShell: Icon = IconLoader.findIcon("/icons/powershell.png", JustIcons::class.java.classLoader)!!
    var Python: Icon = IconLoader.findIcon("/icons/python.svg", JustIcons::class.java.classLoader)!!
    var Ruby: Icon = IconLoader.findIcon("/icons/ruby.svg", JustIcons::class.java.classLoader)!!
    var Bun: Icon = IconLoader.findIcon("/icons/bun.svg", JustIcons::class.java.classLoader)!!
    var DuckDB: Icon = IconLoader.findIcon("/icons/duckdb.png", JustIcons::class.java.classLoader)!!
}