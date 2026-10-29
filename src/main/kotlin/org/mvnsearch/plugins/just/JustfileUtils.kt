package org.mvnsearch.plugins.just

val INDENT_CHARS = listOf(' ', '\t', '\n')
val PARAM_PREFIX_LIST = listOf('$', '+', '*', '@')

fun parseRecipeName(line: String): String {
    return line.substringBefore(':').substringBefore(' ').removePrefix("@")
}

fun removeVariablePrefix(name: String): String {
    if (name.isNotEmpty() && PARAM_PREFIX_LIST.contains(name[0])) {
        return name.substring(1)
    }
    return name
}