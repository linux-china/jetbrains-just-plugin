package org.mvnsearch.plugins.just

val INDENT_CHARS = listOf(' ', '\t', '\n')
val PARAM_PREFIX_LIST = listOf('$', '+', '*', '@')

fun parseRecipeName(line: String): String {
    var recipe = line.substring(0, line.indexOf(':'))
    if (recipe.contains(' ')) {
        recipe = recipe.substring(0, recipe.indexOf(' '))
    }
    if (recipe.startsWith("@")) {
        recipe = recipe.substring(1);
    }
    return recipe
}

fun removeVariablePrefix(name: String): String {
    if (PARAM_PREFIX_LIST.contains(name[0])) {
        return name.substring(1)
    }
    return name
}