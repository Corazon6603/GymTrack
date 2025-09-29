package com.corazon.gymtrack

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corazon.gymtrack.ui.theme.GymBackgroundColor
import com.corazon.gymtrack.ui.theme.GymRed
import com.corazon.gymtrack.ui.theme.GymSecondaryBackgroundColor
import com.corazon.gymtrack.ui.theme.GymTrackTheme
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

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
                MainScreen(viewModel = workoutViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    val workoutList = viewModel.workoutList.value
    val openAddDialog = remember { mutableStateOf(false) }
    var workoutToEdit by remember { mutableStateOf<Workout?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GymBackgroundColor
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopBar(openAddDialog = openAddDialog) },
            bottomBar = { MyBottomNavigationBar() }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                // CORRECTION : On gère le clic sur "Supprimer"
                onDelete = {
                    viewModel.deleteWorkout(workoutToEdit!!.id)
                    workoutToEdit = null
                }
            )
        }
    }
}


@Composable
fun TopBar(openAddDialog: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Entraînements",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { openAddDialog.value = true },
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
fun MyBottomNavigationBar() {
    val items = listOf(
        BottomNavItem(label = "Entraînement", icon = Icons.Default.Home),
        BottomNavItem(label = "Notes", icon = Icons.Default.Edit),
        BottomNavItem(label = "Nutrition", icon = Icons.Default.ShoppingCart),
        BottomNavItem(label = "Stats", icon = Icons.Default.Person)
    )
    var selectedItemIndex by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = GymSecondaryBackgroundColor
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                modifier = Modifier.weight(1f),
                selected = selectedItemIndex == index,
                onClick = { selectedItemIndex = index },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 11.sp) },
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
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GymRed,
                        focusedIndicatorColor = GymRed,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
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
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GymRed,
                        focusedIndicatorColor = GymRed,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        }
    )
}

// CORRECTION : Le dialogue de modification a maintenant un bouton "Supprimer"
@Composable
fun EditWorkoutDialog(
    workout: Workout,
    onDismissRequest: () -> Unit,
    onConfirmation: (newName: String, newDescription: String) -> Unit,
    onDelete: () -> Unit // Ajout du callback pour la suppression
) {
    var nomProg by remember { mutableStateOf(workout.name) }
    var nomDesc by remember { mutableStateOf(workout.description) }
    val isNameValid = nomProg.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        // On utilise une Row pour aligner nos 3 boutons
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Le bouton Supprimer, aligné à gauche
                TextButton(onClick = onDelete) {
                    Text("Supprimer", color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                // 2. Les boutons Annuler et Sauvegarder, à droite
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
        // On laisse dismissButton vide car on gère tout dans confirmButton
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
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GymRed,
                        focusedIndicatorColor = GymRed,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GymTrackTheme {
        val fakeWorkoutList = remember {
            mutableStateOf(
                listOf(
                    Workout(1, "Développé Couché", "Programme force", 10),
                    Workout(2, "Squat", "Programme jambes", 8)
                )
            )
        }
        val openAddDialog = remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GymBackgroundColor
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { TopBar(openAddDialog = openAddDialog) },
                bottomBar = { MyBottomNavigationBar() }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(fakeWorkoutList.value) { workout ->
                        WorkoutCard(workout = workout, onEditClick = {})
                    }
                }
            }
        }
    }
}