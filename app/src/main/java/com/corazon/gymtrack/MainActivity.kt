package com.corazon.gymtrack

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.corazon.gymtrack.ui.theme.GymRed
import com.corazon.gymtrack.ui.theme.GymSecondaryBackgroundColor
import com.corazon.gymtrack.ui.theme.GymTrackTheme
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable


// --- (Placez vos autres data classes comme FoodItem, Meal, etc. ici ou dans leurs propres fichiers) ---
// Par exemple:
// data class FoodItem(...)
// enum class ServingType { ... }
// ...

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Training : Screen("training", "Entraînement", Icons.Filled.Home)
    object Notes : Screen("notes", "Notes", Icons.Filled.Edit)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Filled.ShoppingCart)
    object Stats : Screen("stats", "Stats", Icons.Filled.Person)
}

@Serializable
data class Workout(
    val id: Int,
    val name: String,
    val description: String,
    val weeks: Int
)

@OptIn(InternalSerializationApi::class)
class MainActivity : ComponentActivity() {
    private val workoutViewModel: WorkoutViewModel by viewModels {
        WorkoutViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymTrackTheme {
                GymTrackApp(workoutViewModel = workoutViewModel)
            }
        }
    }
}

@Composable
fun GymTrackApp(workoutViewModel: WorkoutViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { MyBottomNavigationBar(navController = navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Training.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Training.route) {
                    TrainingScreen(viewModel = workoutViewModel)
                }
                composable(Screen.Notes.route) {
                    NotesScreen()
                }
                composable(Screen.Nutrition.route) {
                    val nutritionViewModel: NutritionViewModel = viewModel(
                        factory = NutritionViewModelFactory(LocalContext.current.applicationContext)
                    )
                    NutritionScreen(viewModel = nutritionViewModel)
                }
                composable(Screen.Stats.route) {
                    StatsScreen()
                }
            }
        }
    }
}

@Composable
fun TrainingScreen(viewModel: WorkoutViewModel) {
    val workoutList = viewModel.workoutList.value
    val openAddDialog = remember { mutableStateOf(false) }
    var workoutToEdit by remember { mutableStateOf<Workout?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Entraînements",
            showAddButton = true,
            onAddClick = { openAddDialog.value = true }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(workoutList) { workout ->
                WorkoutCard(
                    workout = workout,
                    onEditClick = { workoutToEdit = workout }
                )
            }
        }
    }

    if (openAddDialog.value) {
        AlertDialogExample(
            onDismissRequest = { openAddDialog.value = false },
            onConfirmation = { name, description, weeks ->
                viewModel.addWorkout(name, description, weeks)
                openAddDialog.value = false
            }
        )
    }

    if (workoutToEdit != null) {
        EditWorkoutDialog(
            workout = workoutToEdit!!,
            onDismissRequest = { workoutToEdit = null },
            onConfirmation = { newName, newDescription ->
                viewModel.updateWorkout(workoutToEdit!!.id, newName, newDescription)
                workoutToEdit = null
            },
            onDelete = {
                viewModel.deleteWorkout(workoutToEdit!!.id)
                workoutToEdit = null
            }
        )
    }
}

@Composable
fun NutritionScreen(viewModel: NutritionViewModel) {
    var showCreateFoodDialog by remember { mutableStateOf(false) }
    var showFoodListDialog by remember { mutableStateOf(false) }
    var foodToEdit by remember { mutableStateOf<FoodItem?>(null) }
    var categoryToCreateMealIn by remember { mutableStateOf<String?>(null) }
    var mealToEdit by remember { mutableStateOf<Meal?>(null) }
    var showSetGoalsDialog by remember { mutableStateOf(false) }
    var nutrientToAddValueTo by remember { mutableStateOf<NutrientData?>(null) }

    // On récupère toutes les listes du ViewModel
    val nutrients = viewModel.nutrients.value
    val mealCategories = viewModel.mealCategories.value
    val expandedCategory = viewModel.expandedCategory.value
    val foodList = viewModel.foodList.value
    val mealList = viewModel.mealList.value
    val consumedMealList = viewModel.consumedMealList.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1 : OBJECTIFS QUOTIDIENS ---
        item {
            TopBar(
                title = "Objectifs Quotidiens",
                showAddButton = false,
                onResetClick = { viewModel.resetDailyValues() }
            )
        }
        item {
            Button(
                onClick = { showSetGoalsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GymRed.copy(alpha = 0.8f))
            ) { Text("Définir les Objectifs") }
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(nutrients) { nutrient ->
            NutrientCard(
                nutrient = nutrient,
                onAddClick = { nutrientToAddValueTo = nutrient }
            )
        }

        // --- SECTION 2 : REPAS CONSOMMÉS (NOUVEAU) ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Repas Consommés", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (consumedMealList.isEmpty()) {
            item {
                Text(
                    text = "Aucun repas consommé pour le moment. Cliquez sur le '+' à côté d'un de vos repas pour l'ajouter.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(consumedMealList) { consumedMeal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(consumedMeal.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Kcal: ${consumedMeal.totalCalories}, Prot: ${consumedMeal.totalProtein}g, Gluc: ${consumedMeal.totalCarbs}g",
                                color = Color.LightGray, fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { viewModel.deleteConsumedMeal(consumedMeal.id) }) {
                            Icon(Icons.Filled.Delete, "Supprimer le repas consommé", tint = Color.Gray)
                        }
                    }
                }
            }
        }


        // --- SECTION 3 : GESTION DES ALIMENTS ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aliments", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showCreateFoodDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = GymRed)) { Text("Créer un aliment") }
                    TextButton(onClick = { showFoodListDialog = true }) { Text("Voir les aliments", color = Color.Gray) }
                }
            }
        }

        // --- SECTION 4 : GESTION DES MODÈLES DE REPAS ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Mes Repas (Modèles)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        items(mealCategories) { category ->
            MealCategoryCard(
                category = category,
                meals = mealList.filter { it.category == category.label },
                isExpanded = expandedCategory == category.label,
                onClick = { viewModel.onCategoryClicked(category.label) },
                onAddMealClick = { categoryToCreateMealIn = category.label },
                onMealClick = { meal -> mealToEdit = meal },
                onConsumeClick = { meal -> viewModel.consumeMeal(meal) } // Connexion de la nouvelle fonctionnalité
            )
        }
    }

    // --- GESTION DE TOUTES LES BOÎTES DE DIALOGUE ---
    if (showSetGoalsDialog) {
        SetGoalsDialog(
            currentNutrients = nutrients,
            onDismissRequest = { showSetGoalsDialog = false },
            onConfirmation = { c, p, g, h -> viewModel.updateNutrientTargets(c, p, g, h); showSetGoalsDialog = false }
        )
    }
    if (nutrientToAddValueTo != null) {
        AddNutrientValueDialog(
            nutrient = nutrientToAddValueTo!!,
            onDismissRequest = { nutrientToAddValueTo = null },
            onConfirmation = { amount -> viewModel.addNutrientValue(nutrientToAddValueTo!!.label, amount); nutrientToAddValueTo = null }
        )
    }
    if (showCreateFoodDialog) {
        CreateFoodDialog(
            onDismissRequest = { showCreateFoodDialog = false },
            onConfirmation = { name, cal, prot, carb, type, weight -> viewModel.addFood(name, cal, prot, carb, type, weight); showCreateFoodDialog = false }
        )
    }
    if (showFoodListDialog) {
        FoodListDialog(
            foodList = foodList,
            onDismissRequest = { showFoodListDialog = false },
            onFoodClick = { food -> showFoodListDialog = false; foodToEdit = food }
        )
    }
    if (foodToEdit != null) {
        EditFoodDialog(
            foodItem = foodToEdit!!,
            onDismissRequest = { foodToEdit = null },
            onConfirmation = { updatedFood -> viewModel.updateFood(updatedFood); foodToEdit = null },
            onDelete = { foodId -> viewModel.deleteFood(foodId); foodToEdit = null }
        )
    }
    if (categoryToCreateMealIn != null) {
        CreateMealDialog(
            category = categoryToCreateMealIn!!,
            allFoodItems = foodList,
            onDismissRequest = { categoryToCreateMealIn = null },
            onConfirmation = { name, foods -> viewModel.addMeal(name, categoryToCreateMealIn!!, foods); categoryToCreateMealIn = null }
        )
    }
    if (mealToEdit != null) {
        EditMealDialog(
            meal = mealToEdit!!,
            allFoodItems = foodList,
            onDismissRequest = { mealToEdit = null },
            onConfirmation = { id, name, foods -> viewModel.updateMeal(id, name, foods); mealToEdit = null },
            onDelete = { id -> viewModel.deleteMeal(id); mealToEdit = null }
        )
    }
}

@Composable
fun SetGoalsDialog(
    currentNutrients: List<NutrientData>,
    onDismissRequest: () -> Unit,
    onConfirmation: (calories: String, protein: String, carbs: String, hydration: String) -> Unit
) {
    // On pré-remplit les champs avec les objectifs actuels
    var calories by remember { mutableStateOf(currentNutrients.find { it.label == "Calories" }?.targetValue?.toInt().toString()) }
    var protein by remember { mutableStateOf(currentNutrients.find { it.label == "Protéines" }?.targetValue?.toInt().toString()) }
    var carbs by remember { mutableStateOf(currentNutrients.find { it.label == "Glucides" }?.targetValue?.toInt().toString()) }
    var hydration by remember { mutableStateOf(currentNutrients.find { it.label == "Hydratation" }?.targetValue.toString()) }

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )
    val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Définir les Objectifs", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (Kcal)") }, colors = textFieldColors, keyboardOptions = keyboardOptions)
                TextField(value = protein, onValueChange = { protein = it }, label = { Text("Protéines (g)") }, colors = textFieldColors, keyboardOptions = keyboardOptions)
                TextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Glucides (g)") }, colors = textFieldColors, keyboardOptions = keyboardOptions)
                TextField(value = hydration, onValueChange = { hydration = it }, label = { Text("Hydratation (L)") }, colors = textFieldColors, keyboardOptions = keyboardOptions)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmation(calories, protein, carbs, hydration) }) {
                Text("Sauvegarder", color = GymRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )
}

@Composable
fun AddNutrientValueDialog(
    nutrient: NutrientData,
    onDismissRequest: () -> Unit,
    onConfirmation: (amount: Float) -> Unit
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Ajouter: ${nutrient.label}", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            TextField(
                value = value,
                onValueChange = { if (it.matches(Regex("^-?\\d*\\.?\\d*\$"))) value = it },
                label = { Text("Valeur à ajouter (${nutrient.unit})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
                    focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
                )
            )
        },
        confirmButton = {
            val isEnabled = value.toFloatOrNull() != null
            TextButton(
                onClick = { onConfirmation(value.toFloatOrNull() ?: 0f) },
                enabled = isEnabled
            ) {
                Text("Ajouter", color = if (isEnabled) GymRed else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )
}

@Composable
fun MealCategoryCard(
    category: MealCategory,
    meals: List<Meal>,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onAddMealClick: () -> Unit,
    onMealClick: (Meal) -> Unit,
    onConsumeClick: (Meal) -> Unit // Nouveau paramètre pour "consommer" le repas
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
    ) {
        Column {
            // Section cliquable pour déplier/replier la catégorie
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = category.icon, contentDescription = category.label, tint = GymRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = category.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = "Dérouler", tint = Color.Gray)
            }

            // Contenu qui apparaît quand la catégorie est dépliée
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    // Boucle sur les repas de la catégorie (les "modèles" de repas)
                    meals.forEach { meal ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMealClick(meal) } // Pour modifier le repas
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Informations sur le repas (nom, macros)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(meal.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    "Kcal: ${meal.totalCalories}, Prot: ${meal.totalProtein}g, Gluc: ${meal.totalCarbs}g",
                                    color = Color.LightGray, fontSize = 12.sp
                                )
                            }
                            // Bouton "+" pour ajouter ce repas à la consommation du jour
                            IconButton(onClick = { onConsumeClick(meal) }) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircle, // Une icône appropriée
                                    contentDescription = "Consommer ce repas",
                                    tint = GymRed
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Bouton pour créer un nouveau "modèle" de repas dans cette catégorie
                    Button(
                        onClick = onAddMealClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Créer un nouveau repas")
                    }
                }
            }
        }
    }
}

@Composable
fun EditMealDialog(
    meal: Meal,
    allFoodItems: List<FoodItem>,
    onDismissRequest: () -> Unit,
    onConfirmation: (mealId: Long, newName: String, newFoods: List<MealFood>) -> Unit,
    onDelete: (mealId: Long) -> Unit
) {
    var mealName by remember { mutableStateOf(meal.name) }
    var currentFoods by remember { mutableStateOf(meal.foods) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodToModify by remember { mutableStateOf<MealFood?>(null) } // État pour la modification

    val isConfirmEnabled = mealName.isNotBlank() && currentFoods.isNotEmpty()
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )

    // Calcul du total des macros en temps réel
    val mealTotal = remember(currentFoods) {
        var cals = 0f
        var prot = 0f
        var carbs = 0f
        currentFoods.forEach { mealFood ->
            val foodItem = allFoodItems.find { it.id == mealFood.foodItemId }
            if (foodItem != null) {
                val amount = mealFood.amount
                val totalWeight = if (foodItem.servingType == ServingType.PER_UNIT) (foodItem.unitWeight ?: 1f) * amount else amount
                cals += (foodItem.caloriesPer100g / 100) * totalWeight
                prot += (foodItem.proteinPer100g / 100) * totalWeight
                carbs += (foodItem.carbsPer100g / 100) * totalWeight
            }
        }
        Triple(cals, prot, carbs)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Modifier le Repas", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column {
                TextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Nom du repas") },
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(Modifier.height(16.dp))
                Text("Aliments", color = GymRed)

                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(currentFoods) { mealFood ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { foodToModify = mealFood }
                            ) {
                                MealFoodDetails(mealFood = mealFood, allFoodItems = allFoodItems)
                            }
                            IconButton(onClick = { currentFoods = currentFoods - mealFood }) {
                                Icon(Icons.Filled.Delete, "Supprimer", tint = Color.Gray)
                            }
                        }
                    }
                }

                if (currentFoods.isNotEmpty()) {
                    Card(
                        modifier = Modifier.padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Text(
                            text = "Total: ${mealTotal.first.toInt()} kcal, ${mealTotal.second.toInt()}g Prot, ${mealTotal.third.toInt()}g Gluc",
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showAddFoodDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                ) {
                    Text("Ajouter un aliment")
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDelete(meal.id) }) {
                    Text("Supprimer", color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismissRequest) {
                    Text("Annuler", color = GymRed)
                }
                TextButton(
                    onClick = { onConfirmation(meal.id, mealName, currentFoods) },
                    enabled = isConfirmEnabled
                ) {
                    Text("Sauvegarder", color = if (isConfirmEnabled) GymRed else Color.Gray)
                }
            }
        },
        dismissButton = {}
    )

    if (showAddFoodDialog) {
        AddFoodToMealDialog(
            allFoodItems = allFoodItems,
            onDismissRequest = { showAddFoodDialog = false },
            onFoodAdded = { newMealFood ->
                currentFoods = currentFoods + newMealFood
                showAddFoodDialog = false
            }
        )
    }

    if (foodToModify != null) {
        val originalFood = foodToModify!!
        val foodItem = allFoodItems.find { it.id == originalFood.foodItemId }
        if (foodItem != null) {
            EditMealFoodDialog(
                mealFood = originalFood,
                foodItem = foodItem,
                onDismissRequest = { foodToModify = null },
                onConfirmation = { newAmount ->
                    currentFoods = currentFoods.map { if (it == originalFood) it.copy(amount = newAmount) else it }
                    foodToModify = null
                }
            )
        }
    }
}

// DANS MainActivity.kt

@Composable
fun EditMealFoodDialog(
    mealFood: MealFood,
    foodItem: FoodItem, // On a besoin de l'aliment complet pour afficher les infos
    onDismissRequest: () -> Unit,
    onConfirmation: (newAmount: Float) -> Unit
) {
    // Initialise la quantité avec la valeur actuelle
    var newQuantity by remember { mutableStateOf(mealFood.amount.toString()) }

    val label = if (foodItem.servingType == ServingType.PER_UNIT) "Nouvelle quantité (unités)" else "Nouveau poids (en g)"
    val keyboardType = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Modifier: ${foodItem.name}", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            TextField(
                value = newQuantity,
                onValueChange = { text ->
                    if (text.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        newQuantity = text
                    }
                },
                label = { Text(label) },
                keyboardOptions = keyboardType,
                colors = textFieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            val isConfirmEnabled = (newQuantity.toFloatOrNull() ?: 0f) > 0f
            TextButton(
                onClick = { onConfirmation(newQuantity.toFloatOrNull() ?: 0f) },
                enabled = isConfirmEnabled
            ) {
                Text("Confirmer", color = if (isConfirmEnabled) GymRed else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )
}

@Composable
fun NutrientCard(
    nutrient: NutrientData,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // La rangée du haut (icône, titre, valeurs) reste identique
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GymRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = nutrient.icon,
                        contentDescription = nutrient.label,
                        tint = GymRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = nutrient.label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))

                val valueText = if (nutrient.label == "Hydratation") {
                    String.format("%.1f / %.1f %s", nutrient.currentValue, nutrient.targetValue, nutrient.unit)
                } else {
                    "${nutrient.currentValue.toInt()} / ${nutrient.targetValue.toInt()} ${nutrient.unit}"
                }

                Text(
                    text = valueText,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter valeur", tint = GymRed)
                }
            }
            Spacer(modifier = Modifier.size(16.dp))

            // La barre de progression ne change pas (elle se bloquera visuellement à 100%, ce qui est parfait)
            LinearProgressIndicator(
                progress = { nutrient.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GymRed,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.size(4.dp))

            // --- MODIFICATION DE LA LOGIQUE D'AFFICHAGE EN DESSOUS DE LA BARRE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Condition : Si l'objectif est dépassé...
                if (nutrient.currentValue > nutrient.targetValue) {
                    val surplus = nutrient.currentValue - nutrient.targetValue
                    val surplusText = if (nutrient.label == "Hydratation") {
                        String.format("%.1f%s", surplus, nutrient.unit)
                    } else {
                        "${surplus.toInt()}${nutrient.unit}"
                    }
                    // ...on affiche le texte de dépassement.
                    Text(
                        text = "Dépassement de $surplusText",
                        color = GymRed, // En rouge pour attirer l'attention
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Espace pour pousser le pourcentage à droite
                Spacer(modifier = Modifier.weight(1f))

                // Le pourcentage s'affichera maintenant au-delà de 100%
                Text(
                    text = "${(nutrient.progress * 100).toInt()}%",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun NotesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page Notes", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page Stats", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TopBar(
    title: String,
    showAddButton: Boolean,
    onAddClick: () -> Unit = {},
    onResetClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showAddButton) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "+",
                    color = GymRed,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Réinitialiser",
                    tint = GymRed
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(
    workout: Workout,
    onEditClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val borderColor = if (isPressed) GymRed else Color.Transparent

    Card(
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.angryimg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workout.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onEditClick,
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(40.dp)
                            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.menu_dots_vertical_invert),
                            contentDescription = "Modifier entraînement",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = workout.description,
                    color = Color.Gray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Text(
                    text = "${workout.weeks} semaines",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MyBottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Training,
        Screen.Notes,
        Screen.Nutrition,
        Screen.Stats
    )

    NavigationBar(
        containerColor = GymSecondaryBackgroundColor
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                modifier = Modifier.weight(1f),
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(text = screen.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GymRed,
                    selectedTextColor = GymRed,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, description: String, weeks: String) -> Unit,
) {
    var nomProg by remember { mutableStateOf("") }
    var nomDesc by remember { mutableStateOf("") }
    var nbWeek by remember { mutableStateOf("") }

    val isNameValid = nomProg.isNotBlank()
    val isWeeksValid = nbWeek.toIntOrNull() in 1..52
    val isConfirmEnabled = isNameValid && isWeeksValid
    val textFieldColors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onConfirmation(nomProg, nomDesc, nbWeek) },
                enabled = isConfirmEnabled
            ) {
                Text("Confirmer", color = if (isConfirmEnabled) GymRed else Color.Gray)
            }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) } },
        containerColor = GymSecondaryBackgroundColor,
        title = { Text("Nouveau Programme", color = Color.Gray, fontSize = 25.sp) },
        text = {
            Column {
                Text("Nom du programme", color = GymRed, fontSize = 13.sp)
                TextField(value = nomProg, onValueChange = { nomProg = it }, label = { Text("Ex: PPL") }, singleLine = true, isError = !isNameValid, colors = textFieldColors)
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Description du programme", color = GymRed, fontSize = 13.sp)
                TextField(value = nomDesc, onValueChange = { nomDesc = it }, label = { Text("Focus sur les pectoraux... (Optionnel)") }, singleLine = true, colors = textFieldColors)
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Nombre de semaines", color = GymRed, fontSize = 13.sp)
                TextField(
                    value = nbWeek,
                    onValueChange = { newText ->
                        if (newText.all { char -> char.isDigit() } && (newText.toIntOrNull() ?: 0) <= 52) {
                            nbWeek = newText
                        }
                    },
                    label = { Text("Ex: 12") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isWeeksValid,
                    colors = textFieldColors
                )
            }
        }
    )
}

@Composable
fun EditWorkoutDialog(
    workout: Workout,
    onDismissRequest: () -> Unit,
    onConfirmation: (newName: String, newDescription: String) -> Unit,
    onDelete: () -> Unit
) {
    var nomProg by remember { mutableStateOf(workout.name) }
    var nomDesc by remember { mutableStateOf(workout.description) }
    val isNameValid = nomProg.isNotBlank()
    val textFieldColors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDelete) {
                    Text("Supprimer", color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismissRequest) {
                    Text("Annuler", color = GymRed)
                }
                TextButton(
                    onClick = { onConfirmation(nomProg, nomDesc) },
                    enabled = isNameValid
                ) {
                    Text("Sauvegarder", color = if (isNameValid) GymRed else Color.Gray)
                }
            }
        },
        dismissButton = {},
        containerColor = GymSecondaryBackgroundColor,
        title = { Text("Modifier le Programme", color = Color.Gray, fontSize = 25.sp) },
        text = {
            Column {
                Text("Nom du programme", color = GymRed, fontSize = 13.sp)
                TextField(value = nomProg, onValueChange = { nomProg = it }, singleLine = true, isError = !isNameValid, colors = textFieldColors)
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Description du programme", color = GymRed, fontSize = 13.sp)
                TextField(value = nomDesc, onValueChange = { nomDesc = it }, label = { Text("(Optionnel)") }, singleLine = true, colors = textFieldColors)
            }
        }
    )
}

@Composable
fun CreateFoodDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, calories: String, protein: String, carbs: String, servingType: ServingType, unitWeight: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var unitWeight by remember { mutableStateOf("") }
    var servingType by remember { mutableStateOf(ServingType.PER_100G) }

    val isConfirmEnabled = name.isNotBlank() && calories.isNotBlank() && protein.isNotBlank() && carbs.isNotBlank() &&
            (servingType == ServingType.PER_100G || (servingType == ServingType.PER_UNIT && unitWeight.isNotBlank()))

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Créer un Aliment", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = { servingType = ServingType.PER_100G }, colors = ButtonDefaults.buttonColors(containerColor = if(servingType == ServingType.PER_100G) GymRed else Color.DarkGray)) { Text("Par 100g") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { servingType = ServingType.PER_UNIT }, colors = ButtonDefaults.buttonColors(containerColor = if(servingType == ServingType.PER_UNIT) GymRed else Color.DarkGray)) { Text("Par Unité") }
                }
                Spacer(Modifier.size(16.dp))
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nom de l'aliment") }, colors = textFieldColors, singleLine = true)
                AnimatedVisibility(visible = servingType == ServingType.PER_UNIT) {
                    TextField(value = unitWeight, onValueChange = { unitWeight = it }, label = { Text("Poids d'1 unité (en g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors, singleLine = true)
                }
                TextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors, singleLine = true)
                TextField(value = protein, onValueChange = { protein = it }, label = { Text("Protéines (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors, singleLine = true)
                TextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Glucides (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmation(name, calories, protein, carbs, servingType, if (servingType == ServingType.PER_UNIT) unitWeight else null) }, enabled = isConfirmEnabled) {
                Text("Créer", color = if (isConfirmEnabled) GymRed else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )
}

@Composable
fun FoodListDialog(
    foodList: List<FoodItem>,
    onDismissRequest: () -> Unit,
    onFoodClick: (FoodItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Mes Aliments", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Fermer", color = GymRed) }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(foodList) { food ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onFoodClick(food) }
                            .padding(8.dp)
                    ) {
                        Text(food.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        val servingText = if(food.servingType == ServingType.PER_UNIT) "Saisi par unité de ${food.unitWeight?.toInt()}g" else "Saisi par 100g"
                        Text(servingText, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            "Valeurs pour 100g: Kcal: ${food.caloriesPer100g.toInt()}, Prot: ${food.proteinPer100g.toInt()}g, Gluc: ${food.carbsPer100g.toInt()}g",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EditFoodDialog(
    foodItem: FoodItem,
    onDismissRequest: () -> Unit,
    onConfirmation: (FoodItem) -> Unit,
    onDelete: (Long) -> Unit
) {
    // On "dé-normalise" les valeurs pour l'affichage initial
    // Si c'est par 100g, on affiche directement.
    // Si c'est par unité, on recalcule la valeur par unité pour l'afficher.
    val initialCalories = if (foodItem.servingType == ServingType.PER_UNIT) {
        (foodItem.caloriesPer100g / 100 * (foodItem.unitWeight ?: 1f)).toInt().toString()
    } else {
        foodItem.caloriesPer100g.toInt().toString()
    }
    val initialProtein = if (foodItem.servingType == ServingType.PER_UNIT) {
        (foodItem.proteinPer100g / 100 * (foodItem.unitWeight ?: 1f)).toInt().toString()
    } else {
        foodItem.proteinPer100g.toInt().toString()
    }
    val initialCarbs = if (foodItem.servingType == ServingType.PER_UNIT) {
        (foodItem.carbsPer100g / 100 * (foodItem.unitWeight ?: 1f)).toInt().toString()
    } else {
        foodItem.carbsPer100g.toInt().toString()
    }

    // États internes de la boîte de dialogue
    var name by remember { mutableStateOf(foodItem.name) }
    var unitWeight by remember { mutableStateOf(foodItem.unitWeight?.toInt()?.toString() ?: "") }
    var calories by remember { mutableStateOf(initialCalories) }
    var protein by remember { mutableStateOf(initialProtein) }
    var carbs by remember { mutableStateOf(initialCarbs) }

    val isConfirmEnabled = name.isNotBlank() && calories.isNotBlank() && protein.isNotBlank() && carbs.isNotBlank() &&
            (foodItem.servingType == ServingType.PER_100G || unitWeight.isNotBlank())

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )
    val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Modifier l'Aliment", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label={ Text("Nom de l'aliment") }, singleLine = true, colors = textFieldColors)

                // Affiche le champ "Poids d'1 unité" seulement si c'est un aliment par unité
                AnimatedVisibility(visible = foodItem.servingType == ServingType.PER_UNIT) {
                    TextField(value = unitWeight, onValueChange = { unitWeight = it }, label = { Text("Poids d'1 unité (en g)") }, keyboardOptions = keyboardOptions, colors = textFieldColors, singleLine = true)
                }

                val calLabel = if (foodItem.servingType == ServingType.PER_UNIT) "Calories par unité" else "Calories pour 100g"
                val protLabel = if (foodItem.servingType == ServingType.PER_UNIT) "Protéines par unité" else "Protéines pour 100g"
                val carbLabel = if (foodItem.servingType == ServingType.PER_UNIT) "Glucides par unité" else "Glucides pour 100g"

                TextField(value = calories, onValueChange = { calories = it }, label = { Text(calLabel) }, keyboardOptions = keyboardOptions, singleLine = true, colors = textFieldColors)
                TextField(value = protein, onValueChange = { protein = it }, label = { Text(protLabel) }, keyboardOptions = keyboardOptions, singleLine = true, colors = textFieldColors)
                TextField(value = carbs, onValueChange = { carbs = it }, label = { Text(carbLabel) }, keyboardOptions = keyboardOptions, singleLine = true, colors = textFieldColors)
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDelete(foodItem.id) }) { Text("Supprimer", color = Color.Gray) }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
                TextButton(
                    onClick = {
                        // "Re-normalisation" des données avant de sauvegarder
                        val calFloat = calories.toFloatOrNull() ?: 0f
                        val protFloat = protein.toFloatOrNull() ?: 0f
                        val carbFloat = carbs.toFloatOrNull() ?: 0f
                        val weightFloat = unitWeight.toFloatOrNull()

                        var finalCalPer100g = 0f
                        var finalProtPer100g = 0f
                        var finalCarbPer100g = 0f

                        if (foodItem.servingType == ServingType.PER_UNIT && weightFloat != null && weightFloat > 0) {
                            finalCalPer100g = (calFloat / weightFloat) * 100
                            finalProtPer100g = (protFloat / weightFloat) * 100
                            finalCarbPer100g = (carbFloat / weightFloat) * 100
                        } else {
                            finalCalPer100g = calFloat
                            finalProtPer100g = protFloat
                            finalCarbPer100g = carbFloat
                        }

                        val updatedFood = foodItem.copy(
                            name = name,
                            unitWeight = weightFloat,
                            caloriesPer100g = finalCalPer100g,
                            proteinPer100g = finalProtPer100g,
                            carbsPer100g = finalCarbPer100g
                        )
                        onConfirmation(updatedFood)
                    },
                    enabled = isConfirmEnabled
                ) {
                    Text("Sauvegarder", color = if (isConfirmEnabled) GymRed else Color.Gray)
                }
            }
        },
        dismissButton = {}
    )
}


@Composable
fun CreateMealDialog(
    category: String,
    allFoodItems: List<FoodItem>,
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, foods: List<MealFood>) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var tempFoods by remember { mutableStateOf<List<MealFood>>(emptyList()) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodToModify by remember { mutableStateOf<MealFood?>(null) } // État pour la modification

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray
    )

    // Calcul du total des macros en temps réel
    val mealTotal = remember(tempFoods) {
        var cals = 0f
        var prot = 0f
        var carbs = 0f
        tempFoods.forEach { mealFood ->
            val foodItem = allFoodItems.find { it.id == mealFood.foodItemId }
            if (foodItem != null) {
                val amount = mealFood.amount
                // Calcule le poids total (utile pour les unités)
                val totalWeight = if (foodItem.servingType == ServingType.PER_UNIT) (foodItem.unitWeight ?: 1f) * amount else amount
                cals += (foodItem.caloriesPer100g / 100) * totalWeight
                prot += (foodItem.proteinPer100g / 100) * totalWeight
                carbs += (foodItem.carbsPer100g / 100) * totalWeight
            }
        }
        Triple(cals, prot, carbs)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Nouveau Repas ($category)", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column {
                TextField(value = mealName, onValueChange = { mealName = it }, label = { Text("Nom du repas") }, colors = textFieldColors, singleLine = true)
                Spacer(Modifier.height(16.dp))

                Text("Aliments Ajoutés", color = GymRed)
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                    items(tempFoods) { mealFood ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // La zone cliquable pour modifier
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { foodToModify = mealFood }
                            ) {
                                MealFoodDetails(mealFood = mealFood, allFoodItems = allFoodItems)
                            }
                            // Le bouton pour supprimer
                            IconButton(onClick = { tempFoods = tempFoods - mealFood }) {
                                Icon(Icons.Filled.Delete, "Supprimer", tint = Color.Gray)
                            }
                        }
                    }
                }

                // Affiche le total si la liste n'est pas vide
                if (tempFoods.isNotEmpty()) {
                    Card(
                        modifier = Modifier.padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Text(
                            text = "Total: ${mealTotal.first.toInt()} kcal, ${mealTotal.second.toInt()}g Prot, ${mealTotal.third.toInt()}g Gluc",
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showAddFoodDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                ) {
                    Text("Ajouter un aliment")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation(mealName, tempFoods) },
                enabled = mealName.isNotBlank() && tempFoods.isNotEmpty()
            ) {
                Text("Créer le repas", color = if (mealName.isNotBlank() && tempFoods.isNotEmpty()) GymRed else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )

    if (showAddFoodDialog) {
        AddFoodToMealDialog(
            allFoodItems = allFoodItems,
            onDismissRequest = { showAddFoodDialog = false },
            onFoodAdded = { mealFood ->
                tempFoods = tempFoods + mealFood
                showAddFoodDialog = false
            }
        )
    }

    // Affiche la boîte de dialogue de modification si un aliment est sélectionné
    if (foodToModify != null) {
        val originalFood = foodToModify!!
        val foodItem = allFoodItems.find { it.id == originalFood.foodItemId }
        if (foodItem != null) {
            EditMealFoodDialog(
                mealFood = originalFood,
                foodItem = foodItem,
                onDismissRequest = { foodToModify = null },
                onConfirmation = { newAmount ->
                    // Met à jour la quantité dans la liste temporaire
                    tempFoods = tempFoods.map { if (it == originalFood) it.copy(amount = newAmount) else it }
                    foodToModify = null
                }
            )
        }
    }
}

@Composable
fun AddFoodToMealDialog(
    allFoodItems: List<FoodItem>,
    onDismissRequest: () -> Unit,
    onFoodAdded: (MealFood) -> Unit
) {
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var quantity by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val isConfirmEnabled = selectedFood != null && (quantity.toFloatOrNull() ?: 0f) > 0f

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed,
        focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray,
        disabledTextColor = Color.White,
        disabledIndicatorColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        disabledTrailingIconColor = Color.Gray
    )

    val calculatedValues = remember(selectedFood, quantity) {
        val food = selectedFood
        // On s'assure que même une entrée comme "." ou "0." soit gérée
        val amount = quantity.toFloatOrNull() ?: 0f

        if (food == null || amount <= 0f) {
            Triple(0f, 0f, 0f)
        } else {
            val cals: Float
            val prot: Float
            val carbs: Float

            when (food.servingType) {
                ServingType.PER_100G -> {
                    cals = (food.caloriesPer100g / 100) * amount
                    prot = (food.proteinPer100g / 100) * amount
                    carbs = (food.carbsPer100g / 100) * amount
                }
                ServingType.PER_UNIT -> {
                    val weightPerUnit = food.unitWeight ?: 1f
                    cals = (food.caloriesPer100g / 100) * weightPerUnit * amount
                    prot = (food.proteinPer100g / 100) * weightPerUnit * amount
                    carbs = (food.carbsPer100g / 100) * weightPerUnit * amount
                }
            }
            Triple(cals, prot, carbs)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Ajouter un Aliment", color = Color.Gray) },
        containerColor = GymSecondaryBackgroundColor,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    TextField(
                        value = selectedFood?.name ?: "Sélectionner un aliment",
                        onValueChange = {}, readOnly = true, enabled = false,
                        trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        colors = textFieldColors
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(GymSecondaryBackgroundColor)
                    ) {
                        allFoodItems.forEach { food ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(food.name, color = Color.White, fontWeight = FontWeight.Bold)
                                        val detailsText = when(food.servingType) {
                                            ServingType.PER_100G -> "Pour 100g: ${food.caloriesPer100g.toInt()}kcal, ${food.proteinPer100g.toInt()}p, ${food.carbsPer100g.toInt()}g"
                                            ServingType.PER_UNIT -> {
                                                val unitWeight = food.unitWeight ?: 0f
                                                val calPerUnit = (food.caloriesPer100g / 100 * unitWeight).toInt()
                                                val protPerUnit = (food.proteinPer100g / 100 * unitWeight).toInt()
                                                val carbPerUnit = (food.carbsPer100g / 100 * unitWeight).toInt()
                                                "Par unité (${unitWeight.toInt()}g): ${calPerUnit}kcal, ${protPerUnit}p, ${carbPerUnit}g"
                                            }
                                        }
                                        Text(detailsText, fontSize = 12.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedFood = food
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ce champ apparaît uniquement après avoir sélectionné un aliment
                if (selectedFood != null) {
                    val label = if (selectedFood?.servingType == ServingType.PER_UNIT) "Quantité (unités)" else "Poids (en g)"
                    TextField(
                        value = quantity,
                        // --- MODIFICATION POUR ACCEPTER LES DÉCIMALES ---
                        onValueChange = { newText ->
                            // Autorise les chiffres et un seul point.
                            if (newText.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                quantity = newText
                            }
                        },
                        label = { Text(label) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Clavier adapté
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                // --- MODIFICATION POUR UN AFFICHAGE INSTANTANÉ ---
                // Remplacement de AnimatedVisibility par un simple if
                if (calculatedValues.first > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Total: ${calculatedValues.first.toInt()} kcal, ${calculatedValues.second.toInt()}g Prot, ${calculatedValues.third.toInt()}g Gluc",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFood?.let { food ->
                        val amount = quantity.toFloatOrNull() ?: 0f
                        onFoodAdded(MealFood(foodItemId = food.id, amount = amount))
                    }
                },
                enabled = isConfirmEnabled
            ) {
                Text("Ajouter", color = if (isConfirmEnabled) GymRed else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Annuler", color = GymRed) }
        }
    )
}

// Dans MainActivity.kt

@Composable
fun MealFoodDetails(mealFood: MealFood, allFoodItems: List<FoodItem>) {
    // 1. Retrouver l'aliment complet à partir de son ID
    val foodItem = allFoodItems.find { it.id == mealFood.foodItemId }
    if (foodItem == null) return // Sécurité: si l'aliment n'est pas trouvé, on n'affiche rien

    // 2. Calculer les valeurs nutritionnelles pour la quantité donnée
    val calculatedValues = remember(mealFood, foodItem) {
        val amount = mealFood.amount
        var cals = 0f
        var prot = 0f
        var carbs = 0f
        var totalWeight: Float? = null

        when (foodItem.servingType) {
            ServingType.PER_100G -> {
                cals = (foodItem.caloriesPer100g / 100) * amount
                prot = (foodItem.proteinPer100g / 100) * amount
                carbs = (foodItem.carbsPer100g / 100) * amount
                totalWeight = amount
            }
            ServingType.PER_UNIT -> {
                val weightPerUnit = foodItem.unitWeight ?: 1f
                cals = (foodItem.caloriesPer100g / 100) * weightPerUnit * amount
                prot = (foodItem.proteinPer100g / 100) * weightPerUnit * amount
                carbs = (foodItem.carbsPer100g / 100) * weightPerUnit * amount
                totalWeight = weightPerUnit * amount
            }
        }
        // On retourne toutes les infos dont on a besoin
        Triple(Triple(cals, prot, carbs), totalWeight, foodItem.servingType)
    }

    // 3. Construire le texte principal
    val mainText = when (calculatedValues.third) {
        ServingType.PER_100G -> "• ${foodItem.name} (${calculatedValues.second?.toInt()}g)"
        ServingType.PER_UNIT -> {
            val amountInt = mealFood.amount.let { if (it == it.toInt().toFloat()) it.toInt() else it }
            "• ${foodItem.name} ($amountInt unité(s) - ${calculatedValues.second?.toInt()}g)"
        }
    }

    // 4. Afficher le tout dans une Column
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(mainText, color = Color.White)
        Text(
            text = "${calculatedValues.first.first.toInt()} kcal, ${calculatedValues.first.second.toInt()}g Prot, ${calculatedValues.first.third.toInt()}g Gluc",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp) // Petit décalage pour l'alignement
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GymTrackTheme {
        val context = LocalContext.current
        val fakeViewModel = remember { NutritionViewModel(context) }
        NutritionScreen(viewModel = fakeViewModel)
    }
}