package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import java.io.File

/**
 * create justfile by project type: maven, gradle or normal
 *
 * @author linux_china
 */
class NewJustfileAction : AnAction() {
    companion object {
        val JUSTFILE_MAVEN = """
        #!/usr/bin/env just --justfile
        
        # maven build without tests
        build:
           mvn -DskipTests clean package
       
        # dependencies tree for compile
        dependencies:
          mvn dependency:tree -Dscope=compile > dependencies.txt
        
        # display updates
        updates:
          mvn versions:display-dependency-updates > updates.txt    
        """.trimIndent()
        val JUSTFILE_GRADLE = """
                #!/usr/bin/env just --justfile
                
                build:
                  ./gradlew -x test build    
                
                dependencies:
                  ./gradlew -q dependencies --configuration compileClasspath > ./dependencies.txt
                
                # Report up-to-date dependencies by com.github.ben-manes.versions
                updates:
                  ./gradlew dependencyUpdates > updates.txt
                
                wrapper:
                  ./gradlew wrapper --gradle-version=7.4.2
                """.trimIndent()
        val JUSTFILE_CARGO = """
                #!/usr/bin/env just --justfile
                
                release:
                  cargo build --release    
                
                lint:
                  cargo clippy
                
                bin:
                  cargo run --bin bin -- arg1
                
                example:
                  cargo run --example exname -- arg1
                """.trimIndent()
        val JUSTFILE_CMAKE = """
                #!/usr/bin/env just --justfile
                
                release:
                  cmake -S . -B build -D CMAKE_BUILD_TYPE=Release
                  cmake --build build
                """.trimIndent()
        val JUSTFILE_GOLANG = """
                #!/usr/bin/env just --justfile
                
                update:
                  go get -u
                  go mod tidy -v
                """.trimIndent()
        val JUSTFILE = """
                #!/usr/bin/env just --justfile
                
                hello:
                  echo "hello world"
                """.trimIndent()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)!!
        val projectDir = project.guessProjectDir()!!
        val content = if (projectDir.findChild("pom.xml") != null) {
            JUSTFILE_MAVEN
        } else if (projectDir.findChild("gradlew") != null) {
            JUSTFILE_GRADLE
        } else if (projectDir.findChild("Cargo.toml") != null) {
            JUSTFILE_CARGO
        } else if (projectDir.findChild("CMakeLists.txt") != null) {
            JUSTFILE_CMAKE
        } else if (projectDir.findChild("go.mod") != null) {
            JUSTFILE_GOLANG
        } else {
            JUSTFILE
        }
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)!!
        if (psiElement is PsiDirectory || psiElement is PsiFile) {
            val psiDirectory = if (psiElement is PsiFile) {
                psiElement.parent!!
            } else {
                psiElement as PsiDirectory
            }
            val destDir = psiDirectory.virtualFile
            ApplicationManager.getApplication().runWriteAction {
                val justfile = destDir.createChildData(psiElement, "justfile")
                justfile.getOutputStream(psiElement).use {
                    it.write(content.toByteArray())
                    it.flush()
                }
                File(justfile.path).setExecutable(true)
                if ((SystemInfo.isLinux || SystemInfo.isMac)) {
                    File(justfile.path).setExecutable(true)
                }
                FileEditorManager.getInstance(project).openFile(justfile, true)
            }
        }
    }

}