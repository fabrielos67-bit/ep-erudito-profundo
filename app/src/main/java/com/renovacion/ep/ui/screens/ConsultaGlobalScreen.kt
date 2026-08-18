package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ConsultaGlobalScreen(onVolver: () -> Unit) {
    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<VerseResult>>(emptyList()) }
    var errorParsing by remember { mutableStateOf(false) }
    var buscado by remember { mutableStateOf(false) }

    fun ejecutarBusqueda() {
        val referencia = ReferenceParser.parse(consulta)
        if (referencia == null) {
            errorParsing = true
            resultados = emptyList()
            buscado = false
        } else {
            errorParsing = false
            resultados = SourceRegistry.todas.map { it.buscar(referencia) }
            buscado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta Global") },
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
            Text(
                "Busca una referencia bíblica en todas las fuentes registradas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

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
                Button(onClick = { ejecutarBusqueda() }) {
                    Text("Buscar")
                }
                OutlinedButton(onClick = {
                    consulta = "Mateo 24:36"
                    ejecutarBusqueda()
                }) {
                    Text("Mateo 24:36")
                }
                OutlinedButton(onClick = {
                    consulta = "1 Pedro 1:7"
                    ejecutarBusqueda()
                }) {
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

            if (buscado) {
                Text(
                    "Resultados en ${resultados.size} fuente(s):",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(resultados) { resultado ->
                        ResultadoCard(resultado)
                    }
                }
            }
        }
    }
}
