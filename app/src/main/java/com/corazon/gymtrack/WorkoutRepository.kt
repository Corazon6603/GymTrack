package com.corazon.gymtrack

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class WorkoutRepository(private val context: Context) {

    private val fileName = "workouts.json"

    fun saveWorkouts(workouts: List<Workout>) {
        try {
            val jsonString = Json.encodeToString(workouts)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace() // Gère l'erreur comme il se doit
        }
    }

    fun loadWorkouts(): List<Workout> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                emptyList() // Si le fichier n'existe pas, retourne une liste vide
            } else {
                val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
                Json.decodeFromString(jsonString)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList() // En cas d'erreur, retourne une liste vide
        }
    }
}