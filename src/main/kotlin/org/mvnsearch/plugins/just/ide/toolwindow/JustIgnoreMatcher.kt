package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.nio.file.Paths

/**
 * Project-root `.just-plugin-ignore` rules for tool-window discovery only.
 *
 * One Java NIO glob per line; blank lines and `#` comments are ignored.
 * Patterns containing `/` match root-relative paths; patterns without `/` match any Justfile basename.
 * A trailing slash matches a directory's contents; a leading recursive glob also matches the project root.
 * Gitignore negation (`!`) is intentionally not supported.
 */
internal class JustIgnoreMatcher private constructor(
    private val projectRoot: VirtualFile,
    private val rules: List<Rule>,
) {
    fun ignores(file: VirtualFile): Boolean {
        val relativePath = VfsUtilCore.getRelativePath(file, projectRoot, '/') ?: return false
        val path = Paths.get(relativePath)
        val basename = Paths.get(file.name)
        return rules.any { rule ->
            val candidate = if (rule.matchRelativePath) path else basename
            rule.matchers.any { it.matches(candidate) }
        }
    }

    private data class Rule(val matchRelativePath: Boolean, val matchers: List<PathMatcher>)

    companion object {
        const val FILE_NAME = ".just-plugin-ignore"
        private val log = Logger.getInstance(JustIgnoreMatcher::class.java)

        /** Must be called under a read action, as part of Justfile discovery. */
        fun fromProjectRoot(root: VirtualFile): JustIgnoreMatcher? {
            val ignoreFile = root.findChild(FILE_NAME)?.takeUnless { it.isDirectory } ?: return null
            val contents = try {
                VfsUtilCore.loadText(ignoreFile)
            } catch (e: IOException) {
                log.warn("Cannot read $FILE_NAME at ${ignoreFile.path}", e)
                return null
            }
            val rules = contents.lineSequence().mapNotNull(::parseRule).toList()
            return JustIgnoreMatcher(root, rules)
        }

        private fun parseRule(line: String): Rule? {
            val pattern = line.trim()
            if (pattern.isEmpty() || pattern.startsWith("#")) return null
            if (pattern.startsWith("!")) {
                log.warn("Unsupported negation in $FILE_NAME: $pattern")
                return null
            }

            // A leading slash anchors a pattern to the project root. A trailing slash denotes a directory.
            val rootRelative = pattern.contains('/')
            val normalized = pattern.removePrefix("/").let { if (it.endsWith('/')) "${it}**" else it }
            val globs = if (normalized.startsWith("**/")) {
                listOf(normalized, normalized.removePrefix("**/"))
            } else {
                listOf(normalized)
            }
            val matchers = try {
                globs.map { FileSystems.getDefault().getPathMatcher("glob:$it") }
            } catch (e: IllegalArgumentException) {
                log.warn("Invalid glob in $FILE_NAME: $pattern", e)
                return null
            }
            return Rule(rootRelative, matchers)
        }
    }
}
