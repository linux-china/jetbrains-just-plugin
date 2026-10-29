package org.mvnsearch.plugins.just.ide.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import java.io.File
import java.io.IOException

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
       
        # dump compile dependencies to dependencies.txt
        dependencies:
          mvn dependency:tree -Dscope=compile > dependencies.txt
        
        # dump dependencies updates to updates.txt
        updates:
          mvn versions:display-dependency-updates > updates.txt    
        """.trimIndent()
        val JUSTFILE_MAVEN_SB = """
        #!/usr/bin/env just --justfile
        
        # maven build without tests
        build:
           mvn -DskipTests clean package
        
        # start spring boot
        start:
           mvn -DskipTests spring-boot:run
                   
        # dump compile dependencies to dependencies.txt
        dependencies:
          mvn dependency:tree -Dscope=compile > dependencies.txt
        
        # dump dependencies updates to updates.txt
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
          ./gradlew wrapper --gradle-version=8.10.1
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
        val JUSTFILE_NODEJS = """
        #!/usr/bin/env just --justfile
        export PATH := join(justfile_directory(), "node_modules", ".bin") + PATH_SEP + env_var('PATH')
        
        build:
          npm run build
        """.trimIndent()
        val JUSTFILE_UV = """
         #!/usr/bin/env just --justfile
         export PATH := join(justfile_directory(), ".venv", if os_family() == "windows" { "Scripts" } else { "bin" }) + PATH_SEP + env_var('PATH')
         
         upgrade:
           uv lock --upgrade
         """.trimIndent()
        val JUSTFILE_ZIG = """
         #!/usr/bin/env just --justfile
         
         build:
           zig build --summary all
         
         run:
           zig build run --summary all
         """.trimIndent()
        val JUSTFILE = """
        #!/usr/bin/env just --justfile
        
        hello:
          echo "hello world"
        """.trimIndent()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabledAndVisible = e.place == ActionPlaces.PROJECT_VIEW_POPUP
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val projectDir = project.guessProjectDir() ?: return
        val pomXml = projectDir.findChild("pom.xml")
        val content = if (pomXml != null) {
            if (pomXml.readText().contains("spring-boot-starter")) {
                JUSTFILE_MAVEN_SB
            } else {
                JUSTFILE_MAVEN
            }
        } else if (projectDir.findChild("gradlew") != null) {
            JUSTFILE_GRADLE
        } else if (projectDir.findChild("Cargo.toml") != null) {
            JUSTFILE_CARGO
        } else if (projectDir.findChild("CMakeLists.txt") != null) {
            JUSTFILE_CMAKE
        } else if (projectDir.findChild("go.mod") != null) {
            JUSTFILE_GOLANG
        } else if (projectDir.findChild("package.json") != null) {
            JUSTFILE_NODEJS
        } else if (projectDir.findChild("uv.lock") != null) {
            JUSTFILE_UV
        } else if (projectDir.findChild("build.zig") != null) {
            JUSTFILE_ZIG
        } else {
            JUSTFILE
        }
        var psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (psiElement == null) {
            psiElement = e.getData(CommonDataKeys.PSI_FILE)
        }
        if (psiElement != null && (psiElement is PsiDirectory || psiElement is PsiFile)) {
            val psiDirectory = if (psiElement is PsiFile) {
                psiElement.parent ?: return
            } else {
                psiElement as PsiDirectory
            }
            // just matches `justfile`/`.justfile` case-insensitively, so any existing variant counts
            val justFile = psiDirectory.virtualFile.children.firstOrNull {
                !it.isDirectory && (it.name.equals("justfile", true) || it.name.equals(".justfile", true))
            }
            if (justFile == null) {
                val destDir = psiDirectory.virtualFile
                val newJustfile = try {
                    WriteAction.compute<VirtualFile, IOException> {
                        val file = destDir.createChildData(this, "Justfile")
                        VfsUtil.saveText(file, content)
                        file
                    }
                } catch (ex: IOException) {
                    Messages.showErrorDialog(project, "Failed to create Justfile: ${ex.message}", "New Justfile")
                    return
                }
                if (SystemInfo.isUnix) {
                    File(newJustfile.path).setExecutable(true)
                }
                FileEditorManager.getInstance(project).openFile(newJustfile, true)
            } else {
                FileEditorManager.getInstance(project).openFile(justFile, true)
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

}