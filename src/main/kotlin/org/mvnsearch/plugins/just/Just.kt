package org.mvnsearch.plugins.just

import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import java.io.File
import kotlin.io.path.Path

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
}