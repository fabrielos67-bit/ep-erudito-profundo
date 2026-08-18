package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.ReferenceParser
import com.renovacion.ep.core.SourceRegistry
import com.renovacion.ep.core.VerseResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(sourceId: String, onVolver: () -> Unit) {
    val fuente = SourceRegistry.porId(sourceId)
    var consulta by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<VerseResult?>(null) }
    var errorParsing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fuente?.nombre ?: "Módulo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            if (fuente == null) {
                Text("Fuente no encontrada.")
                return@Column
            }

            Text(
                text = "${fuente.idioma} · ${fuente.periodo}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = consulta,
                onValueChange = {
                    consulta = it
                    errorParsing = false
                },
                label = { Text("Referencia (p. ej. Mateo 24:36)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val referencia = ReferenceParser.parse(consulta)
                    if (referencia == null) {
                        errorParsing = true
                        resultado = null
                    } else {
                        resultado = fuente.buscar(referencia)
                    }
                }) {
                    Text("Buscar")
                }
                OutlinedButton(onClick = { consulta = "Mateo 24:36" }) {
                    Text("Mateo 24:36")
                }
                OutlinedButton(onClick = { consulta = "1 Pedro 1:7" }) {
                    Text("1 Pedro 1:7")
                }
            }

            Spacer(Modifier.height(20.dp))

            if (errorParsing) {
                Text(
                    "No se pudo interpretar la referencia. Usa el formato \"Libro capítulo:verso\".",
                    color = MaterialTheme.colorScheme.error
                )
            }

            resultado?.let { ResultadoCard(it) }
        }
    }
}

@Composable
fun ResultadoCard(resultado: VerseResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = resultado.referencia,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            if (resultado.disponible && resultado.texto != null) {
                Text(resultado.texto, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    "No disponible en ${resultado.fuenteNombre}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            resultado.nota?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
