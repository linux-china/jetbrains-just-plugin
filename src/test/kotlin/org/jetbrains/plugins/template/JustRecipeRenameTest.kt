package org.jetbrains.plugins.template

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JustRecipeRenameTest : BasePlatformTestCase() {

    fun testRenameRecipeUpdatesReferences() {
        myFixture.configureByText(
            "justfile", """
            alias c := clean

            @cl<caret>ean:
              rm -rf build

            build: clean
              echo build

            all: build (clean)
              echo all
            """.trimIndent() + "\n"
        )
        myFixture.renameElementAtCaret("purge")
        myFixture.checkResult(
            """
            alias c := purge

            @purge:
              rm -rf build

            build: purge
              echo build

            all: build (purge)
              echo all
            """.trimIndent() + "\n"
        )
    }

    fun testRenameFromDependency() {
        myFixture.configureByText(
            "justfile", """
            clean:
              rm -rf build

            build: cle<caret>an
              echo build
            """.trimIndent() + "\n"
        )
        myFixture.renameElementAtCaret("purge")
        myFixture.checkResult(
            """
            purge:
              rm -rf build

            build: purge
              echo build
            """.trimIndent() + "\n"
        )
    }
}
