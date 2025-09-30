package com.corazon.gymtrack

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@Serializable
enum class ServingType {
    PER_100G,
    PER_UNIT
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    // Toutes les valeurs sont normalisées pour 100g
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    // Infos optionnelles pour l'affichage et la logique
    val servingType: ServingType,
    val unitWeight: Float? = null
)