package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renovacion.ep.ui.screens.ConsultaGlobalScreen
import com.renovacion.ep.ui.screens.HomeScreen
import com.renovacion.ep.ui.screens.ModuleScreen
import com.renovacion.ep.ui.theme.EPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EPNavHost()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun EPNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onAbrirModulo = { sourceId -> navController.navigate("modulo/$sourceId") },
                onAbrirConsultaGlobal = { navController.navigate("consulta_global") }
            )
        }
        composable(
            route = "modulo/{sourceId}",
            arguments = listOf(navArgument("sourceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
            ModuleScreen(sourceId = sourceId, onVolver = { navController.popBackStack() })
        }
        composable("consulta_global") {
            ConsultaGlobalScreen(onVolver = { navController.popBackStack() })
        }
    }
}
