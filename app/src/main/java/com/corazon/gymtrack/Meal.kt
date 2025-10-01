package com.corazon.gymtrack

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// Représente un repas complet (ex: "Omelette du matin")
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Meal(
    val id: Long,
    val name: String,
    val category: String, // "Petit-déjeuner", "Déjeuner", etc.
    val foods: List<MealFood>, // La liste des aliments dans ce repas
    // Les totaux sont calculés et sauvegardés avec le repas
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int
)