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
    val ignoreDirective= "# @formatter:off"

    /**
     * `just --fmt` can only format a whole justfile, so no features (e.g. FORMAT_FRAGMENTS) are declared:
     * "Reformat selected fragment" will reformat the entire file.
     */
    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return mutableSetOf()
    }

    override fun canFormat(psiFile: PsiFile): Boolean {
        // use viewProvider.contents to avoid copying the whole file text into a new String
        return psiFile is JustFile && !psiFile.viewProvider.contents.contains(ignoreDirective)
    }

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val justFile: File? = request.getIOFile()
        // check physical file or not
        if (justFile != null) {
            val justCmd: String = Just.getJustCmdAbsolutionPath(request.context.project)
            val params: @NonNls MutableList<String?> = SmartList<String?>()
            params.add("--fmt")
            params.add("--justfile")
            params.add(justFile.path)

            val commandLine: GeneralCommandLine = GeneralCommandLine()
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .withExePath(justCmd)
                .withParameters(params)
                .withEnvironment("JUST_UNSTABLE", "1")
                .withCharset(StandardCharsets.UTF_8)

            return object : FormattingTask {
                @Volatile
                private var handler: OSProcessHandler? = null

                @Volatile
                private var cancelled = false

                override fun run() {
                    if (cancelled) return
                    // OSProcessHandler starts the process in its constructor, so create it only when the task runs
                    val processHandler = try {
                        OSProcessHandler(commandLine)
                    } catch (e: ExecutionException) {
                        request.onError("Just file", e.message ?: e.toString())
                        return
                    }
                    handler = processHandler
                    if (cancelled) {
                        processHandler.destroyProcess()
                        return
                    }
                    processHandler.addProcessListener(object : CapturingProcessAdapter() {
                        override fun processTerminated(event: ProcessEvent) {
                            if (cancelled) return
                            val exitCode: Int = event.exitCode
                            if (exitCode == 0) {
                                val formattedText = justFile.readText(StandardCharsets.UTF_8)
                                request.onTextReady(formattedText)
                            } else {
                                request.onError("Justfile format", output.stderr)
                            }
                        }
                    })
                    processHandler.startNotify()
                }

                override fun cancel(): Boolean {
                    cancelled = true
                    handler?.destroyProcess()
                    return true
                }
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