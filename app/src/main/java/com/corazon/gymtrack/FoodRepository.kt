package com.corazon.gymtrack

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class FoodRepository(private val context: Context) {

    private val fileName = "foods.json"

    fun saveFoods(foods: List<FoodItem>) {
        try {
            val jsonString = Json.encodeToString(foods)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadFoods(): List<FoodItem> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                emptyList()
            } else {
                val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
                Json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}