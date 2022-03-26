package org.mvnsearch.plugins.just.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiManager
import org.mvnsearch.plugins.just.lang.JustLanguage
import org.mvnsearch.plugins.just.parseRecipeName

class JustFile(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, JustLanguage) {
    override fun getFileType() = JustFileType
    override fun toString() = "Justfile"


    fun findRecipeElement(recipe: String): JustRecipeStatement? {
        return this.children
            .filterIsInstance<JustRecipeStatement>()
            .firstOrNull {
                val recipeName = it.recipeName.text
                recipeName == recipe || recipeName == "@${recipe}"
            }
    }


    fun findAllRecipes(): List<String> {
        return this.children
            .filterIsInstance<JustRecipeStatement>()
            .map {
                it.recipeName.text
            }.toList()
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
        return justfileMetadata
    }
}


class JustfileMetadata {
    val recipes = mutableListOf<String>()
    val variables = mutableListOf<String>()
    val envVariables = mutableListOf<String>()
    var dotenvLoad = false
    var variablesExported = false
}
