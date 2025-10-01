package com.corazon.gymtrack

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// Représente un aliment spécifique ajouté à un repas, avec sa quantité
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MealFood(
    val foodItemId: Long, // On ne stocke que l'ID de l'aliment pour garder l'objet léger
    val amount: Float // La quantité (en grammes ou en unités)
)