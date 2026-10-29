package org.mvnsearch.plugins.just

import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.EnvironmentUtil
import java.io.File
import java.io.IOException
import kotlin.io.path.Path

val JUSTFILES_MARKDOWN = listOf("README.md", "AGENTS.md", "CLAUDE.md")
private const val MAX_MARKDOWN_SIZE = 1024 * 1024

object Just {
    fun getJustCmdAbsolutionPath(project: Project): String {
        // environment variable
        EnvironmentUtil.getValue("JUST_EXECUTABLE")?.let {
            return it
        }
        // path macro
        PathMacros.getInstance().getValue("JUST_EXECUTABLE")?.let {
            return it
        }
        project.guessProjectDir()?.let { projectDir ->
            projectDir.findFileByRelativePath(".devenv/profile/bin/just")?.let { return it.path }
        }
        // mise
        val userHome = Path(System.getProperty("user.home"))
        userHome.resolve(".local/share/mise/installs/just/latest/just").toFile().let { justExec ->
            if (justExec.exists()) return justExec.absolutePath
        }
        return if (SystemInfo.isWindows) {
            return "just"
        } else {
            if (File("/usr/local/bin/just").exists()) {
                "/usr/local/bin/just"
            } else if (File("/opt/homebrew/bin/just").exists()) {
                "/opt/homebrew/bin/just"
            } else {
                "just"
            }
        }
    }

    fun isDefaultJustfile(justfileName: String): Boolean {
        return justfileName == "justfile"
                || justfileName == "Justfile"
                || justfileName == ".justfile"
    }

    fun isJustfile(justfileName: String): Boolean {
        return isDefaultJustfile(justfileName) || justfileName.endsWith(".just")
    }

    fun isJustfileMarkdown(justVirtualFile: VirtualFile): Boolean {
        val fileName = justVirtualFile.name
        if (!JUSTFILES_MARKDOWN.contains(fileName) || !justVirtualFile.isValid || justVirtualFile.length > MAX_MARKDOWN_SIZE) {
            return false
        }
        // scan line by line and stop at the first just code block instead of reading the whole file
        return try {
            justVirtualFile.inputStream.bufferedReader().useLines { lines ->
                lines.any { it.trimEnd().endsWith("```just") }
            }
        } catch (_: IOException) {
            false
        }
    }
}