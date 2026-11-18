package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.PsiManager
import org.mvnsearch.plugins.just.lang.psi.JustAttribute
import org.mvnsearch.plugins.just.lang.psi.JustFile
import org.mvnsearch.plugins.just.lang.psi.JustFileType
import org.mvnsearch.plugins.just.lang.psi.JustImportStatement
import org.mvnsearch.plugins.just.lang.psi.JustModStatement
import org.mvnsearch.plugins.just.lang.psi.JustTypes
import com.intellij.util.SystemProperties

/**
 * Collect transitive `import` and `mod` source paths for one root Justfile.
 *
 * Run under a read action, outside VFS change callbacks. Unlike the JSON dump, PSI exposes import
 * declarations; files with arbitrary extensions are parsed as Just syntax in a temporary PSI file.
 * Missing targets are recorded so creating an optional import/module also triggers a refresh.
 */
internal data class JustSourceMetadata(
    val dependencies: Set<String>,
    val privateModules: Set<String>,
)

/** Collect transitive source paths and private module namespaces from `import` and `mod` declarations. */
internal class JustSourceDependencyCollector(private val project: Project) {
    fun collect(root: VirtualFile): JustSourceMetadata {
        val dependencies = mutableSetOf<String>()
        val privateModules = mutableSetOf<String>()
        val visited = mutableSetOf<Pair<String, String>>()
        val activeFiles = mutableSetOf<String>()
        val fileSystem = LocalFileSystem.getInstance()
        val psiManager = PsiManager.getInstance(project)
        val psiFactory = PsiFileFactory.getInstance(project)

        fun resolvePath(parent: VirtualFile, path: String): String {
            val normalized = FileUtil.toSystemIndependentName(path)
            val absolute = when {
                normalized == "~" || normalized.startsWith("~/") ->
                    FileUtil.toSystemIndependentName(SystemProperties.getUserHome()) + normalized.substring(1)
                FileUtil.isAbsolute(normalized) -> normalized
                else -> "${parent.path}/$normalized"
            }
            return normalize(absolute)
        }

        fun visit(file: VirtualFile, modulePath: String) {
            val path = normalize(file.path)
            if (!file.isValid || file.isDirectory || !visited.add(path to modulePath)) return
            // A file may be imported in different module namespaces, but must not recurse into itself.
            if (!activeFiles.add(path)) return
            try {
                val parent = file.parent ?: return
                val psi = psiManager.findFile(file) as? JustFile ?: run {
                    // IntelliJ may assign PlainTextFileType to imports with nonstandard filenames.
                    val contents = try {
                        VfsUtilCore.loadText(file)
                    } catch (_: java.io.IOException) {
                        return
                    }
                    psiFactory.createFileFromText("dependency.just", JustFileType, contents) as? JustFile
                } ?: return

                for (statement in psi.children.filterIsInstance<JustImportStatement>()) {
                    val source = StringUtil.unquoteString(statement.justfilePath.importPath.text)
                    if (source.isBlank()) continue
                    val target = resolvePath(parent, source)
                    dependencies.add(target)
                    fileSystem.findFileByPath(target)?.let { visit(it, modulePath) }
                }

                for (statement in psi.children.filterIsInstance<JustModStatement>()) {
                    val name = statement.modName.text
                    val childPath = if (modulePath.isEmpty()) name else "$modulePath::$name"
                    if (hasPrivateAttribute(statement)) privateModules.add(childPath)
                    val explicit = statement.modPath?.text?.let(StringUtil::unquoteString)
                    val paths = if (explicit.isNullOrBlank()) {
                        (listOf("$name.just") + MODULE_FILES.map { "$name/$it" })
                            .map { resolvePath(parent, it) }
                    } else {
                        val requested = resolvePath(parent, explicit)
                        // Explicit module paths may name either a file or a directory.
                        val child = fileSystem.findFileByPath(requested)
                        when {
                            child != null && !child.isDirectory -> listOf(requested)
                            else -> listOf(requested) + MODULE_FILES.map { normalize("$requested/$it") }
                        }
                    }
                    dependencies.addAll(paths)
                    // In a candidate list, only the first existing source is the selected module.
                    paths.firstNotNullOfOrNull { candidate ->
                        fileSystem.findFileByPath(candidate)?.takeUnless { it.isDirectory }
                    }?.let { visit(it, childPath) }
                }
            } finally {
                activeFiles.remove(path)
            }
        }

        visit(root, "")
        return JustSourceMetadata(dependencies, privateModules)
    }

    /** Attributes precede the declaration as sibling PSI elements (possibly on multiple lines). */
    private fun hasPrivateAttribute(statement: JustModStatement): Boolean {
        var sibling = statement.prevSibling
        while (sibling != null) {
            when {
                sibling is JustAttribute -> {
                    if (sibling.attributeItemList.any { it.attributeName.text == "private" }) return true
                }
                sibling !is PsiWhiteSpace && sibling.elementType != JustTypes.NEW_LINE
                        && sibling.elementType != JustTypes.COMMENT -> return false
            }
            sibling = sibling.prevSibling
        }
        return false
    }

    companion object {
        private val MODULE_FILES = listOf("mod.just", "justfile", "Justfile", ".justfile", ".Justfile")

        fun normalize(path: String): String = FileUtil.toCanonicalPath(FileUtil.toSystemIndependentName(path))
    }
}
