package org.mvnsearch.plugins.just.lang.comment

import com.intellij.lang.Commenter

class JustfileCommenter : Commenter {
    override fun getLineCommentPrefix(): String {
        return "#"
    }

    override fun getBlockCommentPrefix(): String? {
        return null
    }

    override fun getBlockCommentSuffix(): String? {
        return null
    }

    override fun getCommentedBlockCommentPrefix(): String? {
        return null
    }

    override fun getCommentedBlockCommentSuffix(): String? {
        return null
    }
}