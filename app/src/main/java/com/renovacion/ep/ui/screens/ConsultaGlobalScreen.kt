package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renovacion.ep.ui.navigation.MainDrawerLayout
import com.renovacion.ep.ui.navigation.ModuloApp
import com.renovacion.ep.ui.screens.ConsultaGlobalScreen
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
                    var moduloActual by remember { mutableStateOf(ModuloApp.CONSULTA_GLOBAL) }

                    MainDrawerLayout(
                        moduloActual = moduloActual,
                        onSeleccionarModulo = { nuevoModulo ->
                            moduloActual = nuevoModulo
                        }
                    ) { openDrawer ->
                        when (moduloActual) {
                            ModuloApp.CONSULTA_GLOBAL -> {
                                ConsultaGlobalScreen(
                                    onVolver = { },
                                    onOpenDrawer = openDrawer
                                )
                            }
                            ModuloApp.NOTAS -> {
                                PantallaEnConstruccion("Mis Notas (Estilo Keep)", openDrawer)
                            }
                            ModuloApp.FUENTES -> {
                                PantallaEnConstruccion("Biblioteca de Fuentes", openDrawer)
                            }
                            ModuloApp.MARCADORES -> {
                                PantallaEnConstruccion("Marcadores y Guardados", openDrawer)
                            }
                            ModuloApp.CONFIGURACION -> {
                                PantallaEnConstruccion("Ajustes de la Aplicación", openDrawer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PantallaEnConstruccion(
    nombreModulo: String,
    onOpenDrawer: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Módulo en construcción: $nombreModulo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
