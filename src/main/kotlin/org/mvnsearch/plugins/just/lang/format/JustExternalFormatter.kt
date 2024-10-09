package org.mvnsearch.plugins.just.lang.format

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import org.jetbrains.annotations.NonNls
import org.mvnsearch.plugins.just.Just
import org.mvnsearch.plugins.just.lang.psi.JustFile
import java.io.File
import java.nio.charset.StandardCharsets


class JustExternalFormatter : AsyncDocumentFormattingService() {

    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return mutableSetOf()
    }

    override fun canFormat(psiFile: PsiFile): Boolean {
        return psiFile is JustFile
    }

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val justFile: File? = request.getIOFile()
        // check physical file or not
        if (justFile != null) {
            val justCmd: String = Just.getJustCmdAbsolutionPath()
            val params: @NonNls MutableList<String?> = SmartList<String?>()
            params.add("--fmt")
            params.add("--justfile")
            params.add(justFile.getPath())

            try {
                val commandLine: GeneralCommandLine = GeneralCommandLine()
                    .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                    .withExePath(justCmd)
                    .withParameters(params)
                    .withEnvironment("JUST_UNSTABLE", "1")

                val handler = OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8))
                return object : FormattingTask {
                    override fun run() {
                        handler.addProcessListener(object : CapturingProcessAdapter() {
                            override fun processTerminated(event: ProcessEvent) {
                                val exitCode: Int = event.getExitCode()
                                if (exitCode == 0) {
                                    request.context.virtualFile!!.refresh(false, false)
                                } else {
                                    request.onError("Justfile format", getOutput().getStderr())
                                }
                            }
                        })
                        handler.startNotify()
                    }

                    override fun cancel(): Boolean {
                        handler.destroyProcess()
                        return true
                    }

                    override fun isRunUnderProgress(): Boolean {
                        return true
                    }
                }
            } catch (e: ExecutionException) {
                request.onError("Just file", e.message!!)
                return null
            }
        }
        return null
    }

    override fun getNotificationGroupId(): String {
        return "just fmt"
    }

    override fun getName(): String {
        return "just fmt"
    }
}