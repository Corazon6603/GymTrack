// DANS le fichier ConsumedMeal.kt

package com.corazon.gymtrack

import kotlinx.serialization.Serializable

/**
 * Représente une instance d'un repas qui a été consommé.
 * C'est une "photographie" d'un Meal à un instant T.
 */
@Serializable
data class ConsumedMeal(
    val id: Long, // ID unique pour permettre la suppression
    val sourceMealId: Long, // L'ID du Meal original (pour référence future)
    val name: String,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int
)