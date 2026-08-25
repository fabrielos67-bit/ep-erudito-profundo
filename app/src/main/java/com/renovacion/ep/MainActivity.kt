package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.renovacion.ep.ui.KeepHomeScreen
import com.renovacion.ep.ui.navigation.MainDrawerLayout
import com.renovacion.ep.ui.navigation.ModuloApp
import com.renovacion.ep.ui.theme.EPEruditoProfundoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EPEruditoProfundoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var moduloActual by remember { mutableStateOf(ModuloApp.NOTAS) }

                    MainDrawerLayout(
                        moduloActual = moduloActual,
                        onSeleccionarModulo = { nuevoModulo ->
                            moduloActual = nuevoModulo
                        }
                    ) { openDrawer ->
                        KeepHomeScreen(onOpenDrawer = openDrawer)
                    }
                }
            }
        }
    }
}
