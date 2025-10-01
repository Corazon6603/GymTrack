// DANS le fichier UserNutritionData.kt

package com.corazon.gymtrack

import kotlinx.serialization.Serializable

// Un objet pour stocker une valeur actuelle et une cible
@Serializable
data class NutrientValues(
    val current: Float,
    val target: Float
)

// L'objet principal qui sera sauvegardé en JSON
@Serializable
data class UserNutritionData(
    val calories: NutrientValues,
    val protein: NutrientValues,
    val carbs: NutrientValues,
    val hydration: NutrientValues
)