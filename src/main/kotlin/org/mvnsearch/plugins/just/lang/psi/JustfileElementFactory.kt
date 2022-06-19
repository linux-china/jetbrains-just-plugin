package org.mvnsearch.plugins.just.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory


object JustfileElementFactory {
    fun createFile(project: Project, text: String) =
        PsiFileFactory.getInstance(project).createFileFromText("justfile", JustFileType, text) as JustFile


    fun createCodeBlock(project: Project, codeBlock: String): JustCodeBlock {
        val recipeStatement = createFile(project, "name:\n  $codeBlock").firstChild as JustRecipeStatement
        return recipeStatement.dependenciesWithCode?.codeBlock!!
    }

    fun createRecipe(project: Project, recipeName: String): JustRecipeStatement {
        return createFile(project, "${recipeName}:\n").firstChild as JustRecipeStatement
    }

}