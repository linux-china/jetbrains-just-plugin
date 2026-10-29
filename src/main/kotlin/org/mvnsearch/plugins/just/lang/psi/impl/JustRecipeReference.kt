package org.mvnsearch.plugins.just.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.tree.TokenSet
import org.mvnsearch.plugins.just.lang.navigation.JustChooseByRecipeNameContributor.Companion.findRecipeStatements
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustTypes

/**
 * Reference from a recipe name usage (dependency or alias target) to the recipe statement.
 */
class JustRecipeReference(element: PsiElement, private val nameNode: ASTNode) :
    PsiReferenceBase<PsiElement>(
        element,
        TextRange.from(nameNode.startOffset - element.node.startOffset, nameNode.textLength)
    ) {

    override fun resolve(): PsiElement? {
        val recipeName = nameNode.text
        val justFile = element.containingFile as? JustFile ?: return null
        return justFile.findRecipeElement(recipeName)
            ?: findRecipeStatements(element.project, recipeName).firstOrNull()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        (nameNode as LeafElement).replaceWithText(newElementName)
        return element
    }

    companion object {
        fun create(element: PsiElement, nameNode: ASTNode?): JustRecipeReference? {
            // module recipe(mod::recipe) is not supported yet
            if (nameNode == null || nameNode.text.contains("::")) return null
            return JustRecipeReference(element, nameNode)
        }
    }
}

abstract class JustDependencyMixin(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getReference(): PsiReference? {
        val nameNode = node.findChildByType(JustTypes.DEPENDENCY_NAME)
            ?: node.findChildByType(JustTypes.DEPENDENCY_WITH_PARAMS)?.findChildByType(JustTypes.DEPENDENCY_NAME)
        return JustRecipeReference.create(this, nameNode)
    }

    override fun getReferences(): Array<PsiReference> = reference?.let { arrayOf(it) } ?: PsiReference.EMPTY_ARRAY
}

abstract class JustAliasStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getReference(): PsiReference? {
        // alias name := target, `alias b := mod::recipe` is skipped
        val recipeNames = node.getChildren(TokenSet.create(JustTypes.RECIPE_NAME))
        if (recipeNames.size != 2) return null
        return JustRecipeReference.create(this, recipeNames[1])
    }

    override fun getReferences(): Array<PsiReference> = reference?.let { arrayOf(it) } ?: PsiReference.EMPTY_ARRAY
}
