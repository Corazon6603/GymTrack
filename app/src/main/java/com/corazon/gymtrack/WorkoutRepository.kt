package com.corazon.gymtrack

import android.content.Context
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

// On ajoute l'annotation "OptIn" ici pour tout le fichier
@OptIn(InternalSerializationApi::class)
class WorkoutRepository(private val context: Context) {

    private val fileName = "workouts.json"

    fun saveWorkouts(workouts: List<Workout>) {
        try {
            val jsonString = Json.encodeToString(workouts)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadWorkouts(): List<Workout> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                emptyList()
            } else {
                val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
                Json.decodeFromString(jsonString)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}