package com.corazon.gymtrack

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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

data class MealCategory(
    val label: String,
    val icon: ImageVector
)

class NutritionViewModel(context: Context? = null) : ViewModel() {

    private val repository: FoodRepository? = context?.let { FoodRepository(it.applicationContext) }

    val foodList = mutableStateOf<List<FoodItem>>(emptyList())
    val nutrients = mutableStateOf<List<NutrientData>>(loadInitialData())
    val mealCategories = mutableStateOf(
        listOf(
            MealCategory("Petit-déjeuner", Icons.Default.Favorite),
            MealCategory("Déjeuner", Icons.Default.Favorite),
            MealCategory("Collation", Icons.Default.Favorite),
            MealCategory("Dîner", Icons.Default.Favorite)
        )
    )
    val expandedCategory = mutableStateOf<String?>(null)

    init {
        repository?.let {
            foodList.value = it.loadFoods()
        }
    }

    fun addFood(name: String, caloriesStr: String, proteinStr: String, carbsStr: String, servingType: ServingType, unitWeightStr: String?) {
        // ... (La logique de traduction universelle reste ici)
        val calories = caloriesStr.toFloatOrNull() ?: 0f
        val protein = proteinStr.toFloatOrNull() ?: 0f
        val carbs = carbsStr.toFloatOrNull() ?: 0f
        val unitWeight = unitWeightStr?.toFloatOrNull()

        var caloriesPer100g = 0f
        var proteinPer100g = 0f
        var carbsPer100g = 0f

        when (servingType) {
            ServingType.PER_100G -> {
                caloriesPer100g = calories
                proteinPer100g = protein
                carbsPer100g = carbs
            }
            ServingType.PER_UNIT -> {
                if (unitWeight != null && unitWeight > 0) {
                    caloriesPer100g = (calories / unitWeight) * 100
                    proteinPer100g = (protein / unitWeight) * 100
                    carbsPer100g = (carbs / unitWeight) * 100
                }
            }
        }

        val newFood = FoodItem(
            id = System.currentTimeMillis(),
            name = name,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            carbsPer100g = carbsPer100g,
            servingType = servingType,
            unitWeight = unitWeight
        )

        val updatedList = foodList.value + newFood
        foodList.value = updatedList
        repository?.saveFoods(updatedList)
    }

    // NOUVELLE FONCTION : Mettre à jour un aliment
    fun updateFood(updatedFood: FoodItem) {
        val currentList = foodList.value
        val updatedList = currentList.map { food ->
            if (food.id == updatedFood.id) updatedFood else food
        }
        foodList.value = updatedList
        repository?.saveFoods(updatedList)
    }

    // NOUVELLE FONCTION : Supprimer un aliment
    fun deleteFood(foodId: Long) {
        val currentList = foodList.value
        val updatedList = currentList.filter { it.id != foodId }
        foodList.value = updatedList
        repository?.saveFoods(updatedList)
    }

    private fun loadInitialData(): List<NutrientData> {
        return listOf(
            NutrientData("Calories", Icons.Default.Favorite, 1850f, 2200f, "Kcal"),
            NutrientData("Protéines", Icons.Default.Favorite, 75f, 120f, "g"),
            NutrientData("Glucides", Icons.Default.Favorite, 180f, 250f, "g"),
            NutrientData("Hydratation", Icons.Default.Favorite, 1.5f, 2f, "L")
        )
    }

    fun resetDailyValues() {
        val resetList = nutrients.value.map { it.copy(currentValue = 0f) }
        nutrients.value = resetList
    }

    fun onCategoryClicked(categoryLabel: String) {
        expandedCategory.value = if (expandedCategory.value == categoryLabel) null else categoryLabel
    }
}

class NutritionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}