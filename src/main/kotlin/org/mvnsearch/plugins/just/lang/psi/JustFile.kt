package org.mvnsearch.plugins.just.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.parseRecipeName

class JustFile(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, JustLanguage) {
    override fun getFileType() = JustFileType
    override fun toString() = "Justfile"


    fun findRecipeElement(recipe: String): JustRecipeStatement? {
        val currentRecipes = this.children
            .filterIsInstance<JustRecipeStatement>()
            .firstOrNull {
                val recipeName = it.recipeName.text
                recipeName == recipe || recipeName == "@${recipe}"
            }

        if (currentRecipes != null) {
            return currentRecipes
        }

        parseIncludedFiles().forEach { file ->
            val recipeElement = file.findRecipeElement(recipe)
            if (recipeElement != null) {
                return recipeElement
            }
        }

        return null
    }


    fun findAllRecipes(): List<String> {
        return this.children
            .filterIsInstance<JustRecipeStatement>()
            .map {
                it.recipeName.text
            }.toList()
    }

    fun isBashAlike(): Boolean {
        val shellItem = this.children
            .filterIsInstance<JustSetStatement>().firstOrNull() {
                it.setting.text == "shell" || it.setting.text == "windows-powershell"
            }
        return if (shellItem == null) {
            true
        } else {
            shellItem.text.contains("sh\"")
        }
    }

    fun parseMetadata(loadDotenv: Boolean): JustfileMetadata {
        val justfileMetadata = JustfileMetadata()
        text.lines().forEach { line ->
            if (!line.startsWith("#")) {  //skip comments
                if (line.startsWith("export ") && line.contains(":=")) { //export
                    val variable = line.substring(7, line.indexOf(":=")).trim()
                    justfileMetadata.envVariables.add(variable)
                } else if (line.startsWith("set ")) { //set
                    if (line.contains("dotenv-load")) {
                        justfileMetadata.dotenvLoad = line.contains("dotenv-load") && !line.contains("false")
                    } else if (line.contains("export")) {
                        justfileMetadata.variablesExported = !line.contains("false")
                    }
                } else if (line.startsWith("alias ") && line.contains(":=")) {
                    val aliasRecipeName = line.substring(6, line.indexOf(":=")).trim()
                    justfileMetadata.recipes.add(aliasRecipeName)
                } else if (!(line.startsWith(" ") || line.startsWith("\t")) && line.contains(":")) { //recipe or variable
                    if (line.contains(":=")) { // variable
                        val variable = line.substring(0, line.indexOf(":=")).trim()
                        justfileMetadata.variables.add(variable)
                    } else { // recipe
                        justfileMetadata.recipes.add(parseRecipeName(line))
                    }
                }
            }
        }
        if (justfileMetadata.variablesExported) {
            justfileMetadata.envVariables.addAll(justfileMetadata.variables)
            justfileMetadata.variables.clear()
        }
        if (loadDotenv && justfileMetadata.dotenvLoad) {
            val projectDir = project.guessProjectDir()
            projectDir?.findChild(".env")?.let {
                val envFile = PsiManager.getInstance(project).findFile(projectDir.findChild(".env")!!)!!
                envFile.text.lines().forEach { line ->
                    if (!line.startsWith("#") && line.contains("=")) {
                        val variable = line.substring(0, line.indexOf('='))
                        justfileMetadata.envVariables.add(variable)
                    }
                }
            }
        }

        justfileMetadata.imports.addAll(parseIncludedFiles())

        return justfileMetadata
    }

    fun parseIncludedFiles(): List<JustFile> {
        val includedFiles = mutableListOf<JustFile>()
        val importStatements = PsiTreeUtil.getChildrenOfType(this, JustImportStatement::class.java)

        importStatements?.forEach { importStatement ->
            if (importStatement.justfilePath?.importPath != null) {
                val importPath = StringUtil.unquoteString(importStatement.justfilePath?.importPath?.text ?: "")
                if (importPath != "") {
                    val resolvedFile = resolveImportPathToFile(importPath)
                    if (resolvedFile is JustFile) {
                        includedFiles.add(resolvedFile)
                    }
                }
            }
        }

        return includedFiles
    }

    private fun resolveImportPathToFile(importPath: String): PsiFile? {
        if (importPath.isBlank()) {
            return null
        }

        val resolvedFileVirtualFile = if (importPath.startsWith("/")) {
            LocalFileSystem.getInstance().findFileByPath(importPath)
        } else {
            val currentFile = originalFile.virtualFile
            val parentDir = currentFile.parent
            parentDir?.findFileByRelativePath(importPath)
        }

        return resolvedFileVirtualFile?.let { PsiManager.getInstance(project).findFile(it) }
    }
}


class JustfileMetadata {
    val recipes = mutableListOf<String>()
    val variables = mutableListOf<String>()
    val envVariables = mutableListOf<String>()
    val imports = mutableListOf<JustFile>()
    var dotenvLoad = false
    var variablesExported = false
}
