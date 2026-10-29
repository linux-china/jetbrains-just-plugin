package org.mvnsearch.plugins.just

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.EnvironmentUtil
import com.intellij.util.text.VersionComparatorUtil
import java.io.File
import java.io.IOException
import kotlin.io.path.Path

val JUSTFILES_MARKDOWN = listOf("README.md", "AGENTS.md", "CLAUDE.md")
private const val MAX_MARKDOWN_SIZE = 1024 * 1024

object Just {
    fun getJustCmdAbsolutionPath(project: Project): String {
        // environment variable
        EnvironmentUtil.getValue("JUST_EXECUTABLE")?.takeIf { it.isNotBlank() }?.let {
            return it
        }
        // path macro
        PathMacros.getInstance().getValue("JUST_EXECUTABLE")?.takeIf { it.isNotBlank() }?.let {
            return it
        }
        project.guessProjectDir()?.let { projectDir ->
            projectDir.findFileByRelativePath(".devenv/profile/bin/just")?.let { return it.path }
        }
        val userHome = Path(System.getProperty("user.home"))
        val justExecName = if (SystemInfo.isWindows) "just.exe" else "just"
        // mise
        findMiseJust(userHome, justExecName)?.let { return it }
        // PATH, loaded from the login shell by EnvironmentUtil
        if (SystemInfo.isWindows) {
            PathEnvironmentVariableUtil.findExecutableInWindowsPath("just", null)?.let { return it }
        } else {
            PathEnvironmentVariableUtil.findInPath("just")?.let { return it.absolutePath }
        }
        // common install locations
        val candidates = mutableListOf(
            userHome.resolve(".cargo/bin/$justExecName").toFile(),
            userHome.resolve(".local/bin/$justExecName").toFile()
        )
        if (!SystemInfo.isWindows) {
            candidates.add(File("/usr/local/bin/just"))
            candidates.add(File("/opt/homebrew/bin/just"))
        }
        return candidates.firstOrNull { it.isFile && it.canExecute() }?.absolutePath ?: "just"
    }

    private fun findMiseJust(userHome: java.nio.file.Path, justExecName: String): String? {
        val miseDataDir = EnvironmentUtil.getValue("MISE_DATA_DIR")?.takeIf { it.isNotBlank() }?.let { Path(it) }
            ?: userHome.resolve(".local/share/mise")
        val installsDir = miseDataDir.resolve("installs/just").toFile()
        if (!installsDir.isDirectory) return null
        // "latest" symlink first, then the highest installed version
        val versionDirs = listOf(File(installsDir, "latest")) +
                (installsDir.listFiles { file -> file.isDirectory && file.name != "latest" } ?: emptyArray())
                    .sortedWith { a, b -> VersionComparatorUtil.compare(b.name, a.name) }
        for (versionDir in versionDirs) {
            for (justExec in listOf(File(versionDir, justExecName), File(versionDir, "bin/$justExecName"))) {
                if (justExec.isFile) return justExec.absolutePath
            }
        }
        return null
    }

    fun isDefaultJustfile(justfileName: String): Boolean {
        return justfileName == "justfile"
                || justfileName == "Justfile"
                || justfileName == ".justfile"
                || justfileName == ".Justfile"
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