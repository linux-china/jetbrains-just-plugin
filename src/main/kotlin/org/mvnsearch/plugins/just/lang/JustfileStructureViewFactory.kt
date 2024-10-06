package org.mvnsearch.plugins.just.lang

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.mvnsearch.plugins.just.ide.icons.JustIcons
import org.mvnsearch.plugins.just.lang.psi.*


class JustfileStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return JustfileStructureViewModel(editor, psiFile as JustFile)
            }
        }
    }
}

class JustfileStructureViewModel(editor: Editor?, psiFile: JustFile) :
    StructureViewModelBase(psiFile, editor, JustfileStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {
    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean {
        return false
    }

    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement?): Boolean {
        return element?.value is JustRecipeStatement
    }

}

class JustfileStructureViewElement(private val myElement: PsiElement) : StructureViewTreeElement, SortableTreeElement {
    override fun getPresentation(): ItemPresentation {
        if (myElement is JustFile) {
            return PresentationData(myElement.name, "justfile", JustIcons.JUST_FILE, null)
        } else if (myElement is JustRecipeStatement) {
            return PresentationData(myElement.recipeName.text, "Recipe", JustIcons.RUN_ICON, null)
        } else if (myElement is JustVariableStatement) {
            return PresentationData(myElement.variable.text, "Variable", JustIcons.VARIABLE_ICON, null)
        } else if (myElement is JustExportStatement) {
            return PresentationData(myElement.exportName.text, "Export", JustIcons.EXPORT_ICON, null)
        } else if (myElement is JustImportStatement) {
            return PresentationData(myElement.justfilePath.text, "Import", JustIcons.IMPORT_ICON, null)
        } else if (myElement is JustSetStatement) {
            return PresentationData(myElement.setting.text, "Set", JustIcons.SET_ICON, null)
        } else if (myElement is JustModStatement) {
            return PresentationData(myElement.modName.text, "Mod", JustIcons.MOD_ICON, null)
        } else {
            return PresentationData("", "run", JustIcons.JUST_FILE, null)
        }
    }

    override fun getChildren(): Array<TreeElement> {
        if (myElement is JustFile) {
            val recipeStatements: List<PsiElement> = myElement.children.filter {
                it is JustRecipeStatement || it is JustVariableStatement || it is JustExportStatement
            }
            val treeElements: MutableList<TreeElement> = ArrayList(recipeStatements.size)
            for (recipeStatement in recipeStatements) {
                treeElements.add(JustfileStructureViewElement(recipeStatement))
            }
            return treeElements.toTypedArray()
        }
        return emptyArray()
    }

    override fun navigate(requestFocus: Boolean) {
        OpenFileDescriptor(myElement.project, myElement.containingFile.virtualFile, myElement.textOffset).navigate(
            requestFocus
        )
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun canNavigateToSource(): Boolean {
        return true
    }

    override fun getValue(): Any {
        return myElement
    }

    override fun getAlphaSortKey(): String {
        if (myElement is JustRecipeStatement) {
            myElement.recipeName.text
        }
        return ""
    }

}