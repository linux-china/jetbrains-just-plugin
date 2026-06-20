package org.mvnsearch.plugins.just.ide.spellchecker

import com.intellij.spellchecker.BundledDictionaryProvider

class JustDictionaryProvider: BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<out String> {
       return arrayOf("/dictionaries/just.dic")
    }
}