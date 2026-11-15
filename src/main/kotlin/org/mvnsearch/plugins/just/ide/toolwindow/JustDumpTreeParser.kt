package org.mvnsearch.plugins.just.ide.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.mvnsearch.plugins.just.lang.run.RecipeParam

/**
 * Convert a compiled `just --dump --dump-format json` document to tool-window nodes.
 * Imported recipes are already merged into their module's `recipes` by just; `modules` are separate namespaces.
 */
internal class JustDumpTreeParser(
    private val project: Project,
    private val rootJustfile: VirtualFile,
) {
    fun parse(root: JsonObject): JustfileContents = parseModule(root, "")

    private fun parseModule(json: JsonObject, modulePath: String): JustfileContents {
        val recipes = json["recipes"]?.jsonObject?.mapNotNull recipeMap@{ (name, element) ->
            val recipe = element.jsonObject
            if (recipe["private"]?.jsonPrimitive?.booleanOrNull == true) return@recipeMap null

            val description = (recipe["doc"] as? JsonPrimitive)?.contentOrNull
            val params = recipe["parameters"]?.jsonArray?.mapNotNull parameterMap@{ parameterElement ->
                val parameter = parameterElement.jsonObject
                val paramName = (parameter["name"] as? JsonPrimitive)?.contentOrNull ?: return@parameterMap null
                val kind = (parameter["kind"] as? JsonPrimitive)?.contentOrNull
                // Defaults can be literal strings or expressions; preserve the existing display behavior.
                val defaultValue = when (val default = parameter["default"]) {
                    null, is JsonNull -> null
                    is JsonPrimitive -> default.content
                    else -> default.toString()
                }
                RecipeParam(when (kind) {
                    "plus" -> "+$paramName"
                    "star" -> "*$paramName"
                    else -> paramName
                }, defaultValue)
            } ?: emptyList()

            RecipeNode(
                project = project,
                file = rootJustfile,
                recipe = name,
                description = description,
                modulePath = modulePath,
                params = params,
                attributes = recipe["attributes"]?.jsonArray?.toList() ?: emptyList(),
            )
        }?.sortedBy { it.recipe } ?: emptyList()

        val modules = json["modules"]?.jsonObject?.map { (name, element) ->
            val moduleJson = element.jsonObject
            val childPath = if (modulePath.isEmpty()) name else "$modulePath::$name"
            ModuleNode(
                project = project,
                file = rootJustfile,
                modulePath = childPath,
                sourcePath = (moduleJson["source"] as? JsonPrimitive)?.contentOrNull,
                description = (moduleJson["doc"] as? JsonPrimitive)?.contentOrNull,
                contents = parseModule(moduleJson, childPath),
            )
        }?.sortedBy { it.name } ?: emptyList()

        return JustfileContents(recipes, modules)
    }
}
