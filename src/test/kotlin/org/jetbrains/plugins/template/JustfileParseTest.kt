package org.jetbrains.plugins.template

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class JustfileParseTest : BasePlatformTestCase() {

    fun testParseJustfile() {
        val psiFile = myFixture.configureByFile("justfile")
        assertNotNull(psiFile)
    }

    override fun getTestDataPath() = "src/test/testData"

}
