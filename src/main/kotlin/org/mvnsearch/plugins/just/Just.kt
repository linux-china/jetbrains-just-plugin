package org.mvnsearch.plugins.just

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File

object Just {
    fun getJustCmdAbsolutionPath(project: Project): String {
        return if (SystemInfo.isWindows) {
            return "just"
        } else {
            if (File("/usr/local/bin/just").exists()) {
                "/usr/local/bin/just"
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