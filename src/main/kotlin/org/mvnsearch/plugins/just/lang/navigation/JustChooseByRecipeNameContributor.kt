package org.mvnsearch.plugins.just.lang.navigation

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustFileType
import org.mvnsearch.plugins.just.lang.psi.JustRecipeStatement


class JustChooseByRecipeNameContributor : ChooseByNameContributor {
    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String> {
        val names = mutableListOf<String>()
        findWitFiles(project).forEach {
            names.addAll(it.findAllRecipes())
        }
        return names.toTypedArray()
    }

    override fun getItemsByName(
        name: String,
        pattern: String?,
        project: Project,
        includeNonProjectItems: Boolean
    ): Array<NavigationItem> {
        return findRecipeStatements(project, name).toTypedArray()
    }

    private fun findWitFiles(project: Project): List<JustFile> {
        val result = mutableListOf<JustFile>()
        FileTypeIndex.getFiles(JustFileType, GlobalSearchScope.allScope(project)).forEach {
            val witFile: JustFile? = PsiManager.getInstance(project).findFile(it) as JustFile?
            if (witFile != null) {
                result.add(witFile)
            }
        }
        return result
    }

    private fun findRecipeStatements(project: Project, recipeName: String): List<JustRecipeStatement> {
        val result = mutableListOf<JustRecipeStatement>()
        FileTypeIndex.getFiles(JustFileType, GlobalSearchScope.allScope(project)).forEach {
            val witFile = PsiManager.getInstance(project).findFile(it) as JustFile
            witFile.children.filterIsInstance<JustRecipeStatement>().forEach { item ->
                if (item.name == recipeName) {
                    result.add(item)
                }
            }
        }
        return result
    }
}

