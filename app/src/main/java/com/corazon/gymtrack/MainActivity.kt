package com.corazon.gymtrack

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.MutableState
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
import com.corazon.gymtrack.ui.theme.GymBackgroundColor
import com.corazon.gymtrack.ui.theme.GymRed
import com.corazon.gymtrack.ui.theme.GymSecondaryBackgroundColor
import com.corazon.gymtrack.ui.theme.GymTrackTheme
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.io.File

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Training : Screen("training", "Entraînement", Icons.Default.Home)
    object Notes : Screen("notes", "Notes", Icons.Default.Edit)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.ShoppingCart)
    object Stats : Screen("stats", "Stats", Icons.Default.Person)
}

@SuppressLint("UnsafeOptInUsageError")
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
                    val nutritionViewModel: NutritionViewModel = viewModel()
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
    val nutrients = viewModel.nutrients.value
    // On récupère les nouvelles données du ViewModel
    val mealCategories = viewModel.mealCategories.value
    val expandedCategory = viewModel.expandedCategory.value

    // On transforme la page entière en une seule liste défilante
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1 : OBJECTIFS QUOTIDIENS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Objectifs Quotidiens",
                    color = Color.White,
                    fontSize = 15.sp, // Taille de police réduite
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.resetDailyValues() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Réinitialiser",
                        tint = GymRed
                    )
                }
            }
        }

        items(nutrients) { nutrient ->
            NutrientCard(nutrient = nutrient)
        }

        // SECTION 2 : REPAS DE LA JOURNÉE
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Repas de la journée",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(mealCategories) { category ->
            MealCategoryCard(
                category = category,
                // On dit à la carte si elle doit être ouverte ou fermée
                isExpanded = expandedCategory == category.label,
                // On connecte le clic à la fonction du ViewModel
                onClick = { viewModel.onCategoryClicked(category.label) }
            )
        }
    }
}

// --- LE NOUVEAU COMPOSABLE POUR LA CARTE DE REPAS EXTENSIBLE ---
@Composable
fun MealCategoryCard(
    category: MealCategory,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // On rend la carte entière cliquable
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ligne du haut, toujours visible
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.label,
                    tint = GymRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = category.label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                // L'icône change (flèche haut/bas) si la carte est ouverte
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dérouler",
                    tint = Color.Gray
                )
            }

            // Contenu extensible, avec une animation
            AnimatedVisibility(visible = isExpanded) {
                // Ici, tu mettras le contenu de chaque repas (liste d'aliments, etc.)
                // Pour l'instant, on met un simple placeholder.
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ajoutez ici les aliments pour le ${category.label.lowercase()}.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NutrientCard(nutrient: NutrientData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymSecondaryBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                Text(
                    text = "${nutrient.currentValue.toInt()} / ${nutrient.targetValue.toInt()} ${nutrient.unit}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
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
            Text(
                text = "${(nutrient.progress * 100).toInt()}%",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
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
                    imageVector = Icons.Default.AccountCircle,
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
                TextField(
                    value = nomProg,
                    onValueChange = { nomProg = it },
                    label = { Text("Ex: PPL") },
                    singleLine = true,
                    isError = !isNameValid,
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Description du programme", color = GymRed, fontSize = 13.sp)
                TextField(
                    value = nomDesc,
                    onValueChange = { nomDesc = it },
                    label = { Text("Focus sur les pectoraux... (Optionnel)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Nombre de semaines", color = GymRed, fontSize = 13.sp)
                TextField(
                    value = nbWeek,
                    onValueChange = { newText ->
                        if (newText.all { it.isDigit() } && (newText.toIntOrNull() ?: 0) <= 52) {
                            nbWeek = newText
                        }
                    },
                    label = { Text("Ex: 12") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isWeeksValid,
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)
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
                TextField(
                    value = nomProg,
                    onValueChange = { nomProg = it },
                    singleLine = true,
                    isError = !isNameValid,
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("Description du programme", color = GymRed, fontSize = 13.sp)
                TextField(
                    value = nomDesc,
                    onValueChange = { nomDesc = it },
                    label = { Text("(Optionnel)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = GymRed, focusedIndicatorColor = GymRed, unfocusedIndicatorColor = Color.Gray, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray)
                )
            }
        }
    )
}


@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GymTrackTheme {
        // La preview ne peut pas gérer la navigation complexe, donc on affiche un seul écran à la fois.
        // Ici, on prévisualise l'écran de Nutrition
        val fakeNutritionViewModel = remember { NutritionViewModel() }
        NutritionScreen(viewModel = fakeNutritionViewModel)
    }
}