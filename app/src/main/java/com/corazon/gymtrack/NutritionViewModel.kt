package com.corazon.gymtrack

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Modèle pour les objectifs quotidiens
data class NutrientData(
    val label: String,
    val icon: ImageVector,
    var currentValue: Float,
    val targetValue: Float,
    val unit: String
) {
    val progress: Float
        get() = if (targetValue > 0f) currentValue / targetValue else 0f
}

// Modèle pour les catégories de repas
data class MealCategory(
    val label: String,
    val icon: ImageVector
)

// Le "cerveau" complet de la page Nutrition
class NutritionViewModel(context: Context? = null) : ViewModel() {

    private val foodRepository: FoodRepository? = context?.let { FoodRepository(it.applicationContext) }
    private val mealRepository: MealRepository? = context?.let { MealRepository(it.applicationContext) }
    private val nutrientRepository: NutrientRepository? = context?.let { NutrientRepository(it.applicationContext) }
    private val consumedMealRepository: ConsumedMealRepository? = context?.let { ConsumedMealRepository(it.applicationContext) }

    val consumedMealList = mutableStateOf<List<ConsumedMeal>>(emptyList())
    val foodList = mutableStateOf<List<FoodItem>>(emptyList())
    val mealList = mutableStateOf<List<Meal>>(emptyList())
    val nutrients = mutableStateOf<List<NutrientData>>(emptyList())
    val mealCategories = mutableStateOf(
        listOf(
            MealCategory("Petit-déjeuner", Icons.Filled.BreakfastDining),
            MealCategory("Déjeuner", Icons.Filled.LunchDining),
            MealCategory("Collation", Icons.Filled.BakeryDining),
            MealCategory("Dîner", Icons.Filled.DinnerDining)
        )
    )
    val expandedCategory = mutableStateOf<String?>(null)

    init {
        foodRepository?.let { foodList.value = it.loadFoods() }
        mealRepository?.let { mealList.value = it.loadMeals() }
        consumedMealRepository?.let { consumedMealList.value = it.loadConsumedMeals() }
        loadNutrientData()
    }

    // --- LOGIQUE POUR LA CONSOMMATION DE REPAS ---
    fun consumeMeal(meal: Meal) {
        val consumedMeal = ConsumedMeal(
            id = System.currentTimeMillis(),
            sourceMealId = meal.id,
            name = meal.name,
            totalCalories = meal.totalCalories,
            totalProtein = meal.totalProtein,
            totalCarbs = meal.totalCarbs
        )

        consumedMealList.value = consumedMealList.value + consumedMeal
        consumedMealRepository?.saveConsumedMeals(consumedMealList.value)

        val updatedNutrients = nutrients.value.map {
            when (it.label) {
                "Calories" -> it.copy(currentValue = it.currentValue + consumedMeal.totalCalories)
                "Protéines" -> it.copy(currentValue = it.currentValue + consumedMeal.totalProtein)
                "Glucides" -> it.copy(currentValue = it.currentValue + consumedMeal.totalCarbs)
                else -> it
            }
        }
        nutrients.value = updatedNutrients
        saveNutrientData()
    }

    fun deleteConsumedMeal(consumedMealId: Long) {
        val mealToRemove = consumedMealList.value.find { it.id == consumedMealId } ?: return

        val updatedNutrients = nutrients.value.map {
            when (it.label) {
                "Calories" -> it.copy(currentValue = (it.currentValue - mealToRemove.totalCalories).coerceAtLeast(0f))
                "Protéines" -> it.copy(currentValue = (it.currentValue - mealToRemove.totalProtein).coerceAtLeast(0f))
                "Glucides" -> it.copy(currentValue = (it.currentValue - mealToRemove.totalCarbs).coerceAtLeast(0f))
                else -> it
            }
        }
        nutrients.value = updatedNutrients
        saveNutrientData()

        consumedMealList.value = consumedMealList.value.filter { it.id != consumedMealId }
        consumedMealRepository?.saveConsumedMeals(consumedMealList.value)
    }

    // --- LOGIQUE POUR LES DONNÉES NUTRITIONNELLES (OBJECTIFS) ---
    private fun loadNutrientData() {
        val savedData = nutrientRepository?.loadNutrientData()
        if (savedData != null) {
            nutrients.value = listOf(
                NutrientData("Calories", Icons.Filled.LocalFireDepartment, savedData.calories.current, savedData.calories.target, "Kcal"),
                NutrientData("Protéines", Icons.Filled.Restaurant, savedData.protein.current, savedData.protein.target, "g"),
                NutrientData("Glucides", Icons.Filled.Fastfood, savedData.carbs.current, savedData.carbs.target, "g"),
                NutrientData("Hydratation", Icons.Filled.WaterDrop, savedData.hydration.current, savedData.hydration.target, "L")
            )
        } else {
            nutrients.value = getDefaultNutrientData()
        }
    }

    private fun saveNutrientData() {
        if (nutrientRepository == null || nutrients.value.size < 4) return
        val dataToSave = UserNutritionData(
            calories = NutrientValues(nutrients.value[0].currentValue, nutrients.value[0].targetValue),
            protein = NutrientValues(nutrients.value[1].currentValue, nutrients.value[1].targetValue),
            carbs = NutrientValues(nutrients.value[2].currentValue, nutrients.value[2].targetValue),
            hydration = NutrientValues(nutrients.value[3].currentValue, nutrients.value[3].targetValue)
        )
        nutrientRepository.saveNutrientData(dataToSave)
    }

    fun updateNutrientTargets(calories: String, protein: String, carbs: String, hydration: String) {
        val updatedList = nutrients.value.map { nutrient ->
            when (nutrient.label) {
                "Calories" -> nutrient.copy(targetValue = calories.toFloatOrNull() ?: nutrient.targetValue)
                "Protéines" -> nutrient.copy(targetValue = protein.toFloatOrNull() ?: nutrient.targetValue)
                "Glucides" -> nutrient.copy(targetValue = carbs.toFloatOrNull() ?: nutrient.targetValue)
                "Hydratation" -> nutrient.copy(targetValue = hydration.toFloatOrNull() ?: nutrient.targetValue)
                else -> nutrient
            }
        }
        nutrients.value = updatedList
        saveNutrientData()
    }

    fun addNutrientValue(label: String, amount: Float) {
        val updatedList = nutrients.value.map { nutrient ->
            if (nutrient.label == label) {
                val newValue = (nutrient.currentValue + amount).coerceAtLeast(0f)
                nutrient.copy(currentValue = newValue)
            } else {
                nutrient
            }
        }
        nutrients.value = updatedList
        saveNutrientData()
    }

    fun resetDailyValues() {
        consumedMealList.value = emptyList()
        consumedMealRepository?.saveConsumedMeals(emptyList())

        val resetList = nutrients.value.map { it.copy(currentValue = 0f) }
        nutrients.value = resetList
        saveNutrientData()
    }

    private fun getDefaultNutrientData(): List<NutrientData> {
        return listOf(
            NutrientData("Calories", Icons.Filled.LocalFireDepartment, 0f, 2200f, "Kcal"),
            NutrientData("Protéines", Icons.Filled.Restaurant, 0f, 120f, "g"),
            NutrientData("Glucides", Icons.Filled.Fastfood, 0f, 250f, "g"),
            NutrientData("Hydratation", Icons.Filled.WaterDrop, 0f, 2f, "L")
        )
    }

    // --- LOGIQUE POUR LES ALIMENTS (FOODS) ---
    fun addFood(name: String, caloriesStr: String, proteinStr: String, carbsStr: String, servingType: ServingType, unitWeightStr: String?) {
        val calories = caloriesStr.toFloatOrNull() ?: 0f; val protein = proteinStr.toFloatOrNull() ?: 0f; val carbs = carbsStr.toFloatOrNull() ?: 0f; val unitWeight = unitWeightStr?.toFloatOrNull()
        var caloriesPer100g = 0f; var proteinPer100g = 0f; var carbsPer100g = 0f
        when (servingType) {
            ServingType.PER_100G -> { caloriesPer100g = calories; proteinPer100g = protein; carbsPer100g = carbs }
            ServingType.PER_UNIT -> { if (unitWeight != null && unitWeight > 0) { caloriesPer100g = (calories / unitWeight) * 100; proteinPer100g = (protein / unitWeight) * 100; carbsPer100g = (carbs / unitWeight) * 100 } }
        }
        val newFood = FoodItem(id = System.currentTimeMillis(), name = name, caloriesPer100g = caloriesPer100g, proteinPer100g = proteinPer100g, carbsPer100g = carbsPer100g, servingType = servingType, unitWeight = unitWeight)
        val updatedList = foodList.value + newFood; foodList.value = updatedList; foodRepository?.saveFoods(updatedList)
    }

    fun updateFood(updatedFood: FoodItem) {
        val updatedFoodList = foodList.value.map { if (it.id == updatedFood.id) updatedFood else it }
        foodList.value = updatedFoodList
        foodRepository?.saveFoods(updatedFoodList)

        val newlyUpdatedMealList = mealList.value.map { meal ->
            if (meal.foods.any { it.foodItemId == updatedFood.id }) { recalculateMealTotals(meal, foodList.value) } else { meal }
        }
        mealList.value = newlyUpdatedMealList
        mealRepository?.saveMeals(newlyUpdatedMealList)
    }

    fun deleteFood(foodId: Long) {
        val updatedFoodList = foodList.value.filter { it.id != foodId }
        foodList.value = updatedFoodList
        foodRepository?.saveFoods(updatedFoodList)

        val newlyUpdatedMealList = mealList.value.map { meal ->
            val filteredFoodsInMeal = meal.foods.filter { it.foodItemId != foodId }
            if (filteredFoodsInMeal.size != meal.foods.size) { recalculateMealTotals(meal.copy(foods = filteredFoodsInMeal), foodList.value) } else { meal }
        }.filter { it.foods.isNotEmpty() }

        mealList.value = newlyUpdatedMealList
        mealRepository?.saveMeals(newlyUpdatedMealList)
    }

    // --- LOGIQUE POUR LES REPAS (MEALS) ---
    fun addMeal(name: String, category: String, foodsInMeal: List<MealFood>) {
        val newMealTemplate = Meal(id = System.currentTimeMillis(), name = name, category = category, foods = foodsInMeal, totalCalories = 0, totalProtein = 0, totalCarbs = 0)
        val newMealWithTotals = recalculateMealTotals(newMealTemplate, foodList.value)
        val updatedList = mealList.value + newMealWithTotals
        mealList.value = updatedList
        mealRepository?.saveMeals(updatedList)
    }

    fun updateMeal(mealId: Long, newName: String, newFoods: List<MealFood>) {
        val updatedList = mealList.value.map { meal ->
            if (meal.id == mealId) {
                val updatedMealTemplate = meal.copy(name = newName, foods = newFoods)
                recalculateMealTotals(updatedMealTemplate, foodList.value)
            } else { meal }
        }
        mealList.value = updatedList
        mealRepository?.saveMeals(updatedList)
    }

    fun deleteMeal(mealId: Long) {
        val updatedList = mealList.value.filter { it.id != mealId }
        mealList.value = updatedList
        mealRepository?.saveMeals(updatedList)
    }

    private fun recalculateMealTotals(meal: Meal, allFoods: List<FoodItem>): Meal {
        val allFoodsMap = allFoods.associateBy { it.id }
        var totalCalories = 0f; var totalProtein = 0f; var totalCarbs = 0f
        meal.foods.forEach { mealFood ->
            val foodItem = allFoodsMap[mealFood.foodItemId]
            if (foodItem != null) {
                val amount = mealFood.amount
                val totalWeight = if (foodItem.servingType == ServingType.PER_UNIT) (foodItem.unitWeight ?: 1f) * amount else amount
                totalCalories += (foodItem.caloriesPer100g / 100) * totalWeight
                totalProtein += (foodItem.proteinPer100g / 100) * totalWeight
                totalCarbs += (foodItem.carbsPer100g / 100) * totalWeight
            }
        }
        return meal.copy(totalCalories = totalCalories.toInt(), totalProtein = totalProtein.toInt(), totalCarbs = totalCarbs.toInt())
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