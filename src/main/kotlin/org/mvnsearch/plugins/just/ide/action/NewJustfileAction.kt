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