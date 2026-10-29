package org.mvnsearch.plugins.just.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.util.IncorrectOperationException
import javax.swing.Icon

interface JustNamedElement : PsiNameIdentifierOwner, NavigationItem {
    fun getKey(): String?

    fun getValue(): String?

    override fun getName(): String?

    override fun setName(name: String): PsiElement?

    override fun getNameIdentifier(): PsiElement?

    override fun getPresentation(): ItemPresentation?
}

abstract class JustNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JustNamedElement {

    override fun getName(): String? {
        return null
    }

    override fun setName(name: String): PsiElement? {
        throw IncorrectOperationException("Rename is not supported for ${this.node.elementType}")
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun getPresentation(): ItemPresentation? {
        return null
    }

}

abstract class JustRecipeElementImpl(node: ASTNode) : JustNamedElementImpl(node) {
    override fun getKey(): String? {
        val keyNode: ASTNode? = this.node.findChildByType(JustTypes.RECIPE_NAME)
        return keyNode?.text
            ?.replace("\\\\ ".toRegex(), " ")
            ?.replace("@","")
    }

    override fun getValue(): String? {
        val valueNode: ASTNode? = this.node.findChildByType(JustTypes.RECIPE_NAME)
        return valueNode?.text
    }

    override fun getName(): String? {
        return getKey()
    }

    override fun setName(name: String): PsiElement? {
        val keyNode: ASTNode? = this.node.findChildByType(JustTypes.RECIPE_NAME)
        if (keyNode is LeafElement) {
            // keep `@` prefix for quiet recipe
            val newName = if (keyNode.text.startsWith("@")) "@$name" else name
            keyNode.replaceWithText(newName)
        }
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return this.node.findChildByType(JustTypes.RECIPE_NAME)?.psi
    }

    override fun getPresentation(): ItemPresentation? {
        val presentationText = this.getKey()
        val element = this
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                return presentationText
            }

            override fun getLocationString(): String? {
                return element.containingFile?.name
            }

            override fun getIcon(unused: Boolean): Icon {
                return AllIcons.RunConfigurations.TestState.Run
            }
        }
    }
}
