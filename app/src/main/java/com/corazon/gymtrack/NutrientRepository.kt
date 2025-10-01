// DANS le fichier NutrientRepository.kt

package com.corazon.gymtrack

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class NutrientRepository(private val context: Context) {

    private val fileName = "nutrition_data.json"

    fun saveNutrientData(data: UserNutritionData) {
        try {
            val jsonString = Json.encodeToString(data)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadNutrientData(): UserNutritionData? {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                null // Retourne null s'il n'y a pas de fichier sauvegardé
            } else {
                val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
                Json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}