package com.corazon.gymtrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Tes couleurs personnalisées pour le thème sombre
private val DarkColorScheme = darkColorScheme(
    primary = GymRed,
    secondary = GymSecondaryBackgroundColor,
    background = GymBackgroundColor,
    surface = GymBackgroundColor
)

// Gardons un thème clair par défaut au cas où
private val LightColorScheme = lightColorScheme(
    primary = GymRed,
    secondary = GymSecondaryBackgroundColor,
    background = Color.White,
    surface = Color.White
)

@Composable
fun GymTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Pour ton app, on force le thème sombre
        else -> DarkColorScheme
    }

    // --- C'EST ICI QU'ON AJOUTE LA LOGIQUE DU MODE IMMERSIF ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Rendre les barres transparentes pour que notre contenu s'affiche derrière
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            // On cache les barres système pour le mode immersif
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            // On définit comment elles réapparaissent
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    // --- FIN DE LA PARTIE IMMERSIVE ---

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}