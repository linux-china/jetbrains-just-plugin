package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class JustRunConfigurationOptions : RunConfigurationOptions() {
    private val justfileName: StoredProperty<String?> = string("").provideDelegate(this, "fileName")
    private val justRecipeName: StoredProperty<String?> = string("").provideDelegate(this, "recipeName")
    private val justRecipeArgs: StoredProperty<String?> = string("").provideDelegate(this, "recipeArgs")
    private val justRecipeEnvVariables: StoredProperty<String?> = string("").provideDelegate(this, "envVariables")
    private val justOverrideVariables: StoredProperty<String?> = string("").provideDelegate(this, "overrideVariables")
    private val justDotenvPath: StoredProperty<String?> = string("").provideDelegate(this, "dotenvPath")

    fun getFileName(): String? {
        return justfileName.getValue(this)
    }

    fun setFileName(fileName: String?) {
        justfileName.setValue(this, fileName ?: "")
    }

    fun getRecipeName(): String? {
        return justRecipeName.getValue(this)
    }

    fun setRecipeName(recipeName: String?) {
        justRecipeName.setValue(this, recipeName ?: "")
    }

    fun getRecipeArgs(): String? {
        return justRecipeArgs.getValue(this)
    }

    fun setRecipeArgs(recipeArgs: String?) {
        justRecipeArgs.setValue(this, recipeArgs ?: "")
    }

    fun getEnvVariables(): String? {
        return justRecipeEnvVariables.getValue(this)
    }

    fun setEnvVariables(envVariables: String?) {
        justRecipeEnvVariables.setValue(this, envVariables ?: "")
    }

    fun getOverrideVariables(): String? {
        return justOverrideVariables.getValue(this)
    }

    fun setOverrideVariables(overrideVariables: String?) {
        justOverrideVariables.setValue(this, overrideVariables ?: "")
    }

    fun getDotenvPath(): String? {
        return justDotenvPath.getValue(this)
    }

    fun setDotenvPath(dotenvPath: String?) {
        justDotenvPath.setValue(this, dotenvPath ?: "")
    }

}