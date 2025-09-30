package com.corazon.gymtrack

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star // Collation
import androidx.compose.material.icons.filled.Favorite

// Le "plan" pour une ligne de suivi nutritionnel (ne change pas)
data class NutrientData(
    val label: String,
    val icon: ImageVector,
    var currentValue: Float,
    val targetValue: Float,
    val unit: String
) {
    val progress: Float
        get() = (currentValue / targetValue).coerceIn(0f, 1f)
}

// NOUVEAU "PLAN" : Pour une catégorie de repas
data class MealCategory(
    val label: String,
    val icon: ImageVector
)

class NutritionViewModel : ViewModel() {

    // --- SECTION OBJECTIFS QUOTIDIENS ---
    val nutrients = mutableStateOf<List<NutrientData>>(loadInitialData())

    private fun loadInitialData(): List<NutrientData> {
        return listOf(
                NutrientData("Calories", Icons.Default.Star, 1850f, 2200f, "Kcal"),
            NutrientData("Protéines", Icons.Default.Favorite, 75f, 120f, "g"),
            NutrientData("Glucides", Icons.Default.Star, 180f, 250f, "g"),
            NutrientData("Hydratation", Icons.Default.Star, 1.5f, 2f, "L")
        )
    }

    fun resetDailyValues() {
        val resetList = nutrients.value.map { nutrient ->
            nutrient.copy(currentValue = 0f)
        }
        nutrients.value = resetList
    }


    // --- NOUVELLE SECTION : REPAS DE LA JOURNÉE ---

    // La liste des catégories de repas
    val mealCategories = mutableStateOf(
        listOf(
            MealCategory("Petit-déjeuner", Icons.Default.Star),
            MealCategory("Déjeuner", Icons.Default.Star),
            MealCategory("Collation", Icons.Default.Star),
            MealCategory("Dîner", Icons.Default.Star)
        )
    )

    // La "mémoire" qui se souvient de la catégorie actuellement ouverte (son nom)
    // Si 'null', aucune n'est ouverte.
    val expandedCategory = mutableStateOf<String?>(null)

    // Fonction appelée quand on clique sur une catégorie
    fun onCategoryClicked(categoryLabel: String) {
        // Si on clique sur la catégorie déjà ouverte, on la ferme.
        // Sinon, on ouvre la nouvelle.
        expandedCategory.value = if (expandedCategory.value == categoryLabel) null else categoryLabel
    }
}