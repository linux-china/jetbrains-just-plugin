package org.mvnsearch.plugins.just.lang.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class JustRunConfigurationOptions : RunConfigurationOptions() {
    private val justfileName: StoredProperty<String?> = string("").provideDelegate(this, "justfileName")
    private val justRecipeName: StoredProperty<String?> = string("").provideDelegate(this, "recipeName")
    private val justRecipeArgs: StoredProperty<String?> = string("").provideDelegate(this, "recipeArgs")
    private val justRecipeEnvVariables: StoredProperty<String?> = string("").provideDelegate(this, "envVariables")

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

}