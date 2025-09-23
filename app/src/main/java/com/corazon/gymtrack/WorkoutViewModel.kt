package com.corazon.gymtrack

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WorkoutViewModel(context: Context) : ViewModel() {

    private val repository = WorkoutRepository(context)

    // La liste des entraînements est maintenant ici
    val workoutList = mutableStateOf<List<Workout>>(emptyList())

    init {
        // Au démarrage du ViewModel, on charge les données sauvegardées
        loadWorkouts()
    }

    private fun loadWorkouts() {
        workoutList.value = repository.loadWorkouts()
    }

    fun addWorkout(name: String, description: String, weeks: String) {
        val newWorkout = Workout(
            // On utilise le temps en millisecondes pour un ID unique plus fiable
            id = System.currentTimeMillis().toInt(),
            name = name,
            description = description,
            weeks = weeks.toIntOrNull() ?: 0
        )

        val updatedList = workoutList.value + newWorkout
        workoutList.value = updatedList

        // Après avoir mis à jour la liste, on la sauvegarde
        repository.saveWorkouts(updatedList)
    }

    // NOUVELLE FONCTION POUR LA MODIFICATION
    fun updateWorkout(id: Int, newName: String, newDescription: String) {
        // 1. On récupère la liste actuelle
        val currentList = workoutList.value

        // 2. On trouve l'entraînement à modifier grâce à son 'id' et on le met à jour
        val updatedList = currentList.map { workout ->
            if (workout.id == id) {
                // Si c'est le bon, on crée une copie avec les nouvelles infos
                workout.copy(name = newName, description = newDescription)
            } else {
                // Sinon, on ne change pas cet entraînement
                workout
            }
        }

        // 3. On met à jour la liste dans notre ViewModel, ce qui rafraîchit l'écran
        workoutList.value = updatedList

        // 4. On sauvegarde la nouvelle liste complète dans le fichier
        repository.saveWorkouts(updatedList)
    }
}


class WorkoutViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}