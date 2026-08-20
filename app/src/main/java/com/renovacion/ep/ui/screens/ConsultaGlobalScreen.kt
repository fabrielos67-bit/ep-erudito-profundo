package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.EntradasStore
import com.renovacion.ep.core.ReferenceParser
import com.renovacion.ep.core.SourceRegistry
import com.renovacion.ep.core.TextSource
import com.renovacion.ep.core.VerseResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaGlobalScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<VerseResult>>(emptyList()) }
    var errorParsing by remember { mutableStateOf(false) }
    var buscado by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    fun buscarEnFuente(fuente: TextSource, referencia: com.renovacion.ep.core.Reference): VerseResult {
        val textoGuardado = EntradasStore.obtenerTexto(context, fuente.id, referencia.display())
        return if (textoGuardado != null) {
            VerseResult(
                fuenteId = fuente.id,
                fuenteNombre = fuente.nombre,
                referencia = referencia.display(),
                texto = textoGuardado,
                disponible = true,
                nota = "Entrada agregada por ti."
            )
        } else {
            fuente.buscar(referencia)
        }
    }

    fun ejecutarBusqueda() {
        val referencia = ReferenceParser.parse(consulta)
        if (referencia == null) {
            errorParsing = true
            resultados = emptyList()
            buscado = false
        } else {
            errorParsing = false
            resultados = SourceRegistry.todas.map { buscarEnFuente(it, referencia) }
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar entrada")
            }
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

    if (mostrarDialogo) {
        DialogoNuevaEntradaConFuente(
            onCancelar = { mostrarDialogo = false },
            onGuardar = { fuenteId, referenciaTexto, textoEntrada ->
                EntradasStore.guardar(context, fuenteId, referenciaTexto, textoEntrada)
                mostrarDialogo = false
                if (consulta.isNotBlank()) {
                    ejecutarBusqueda()
                }
            }
        )
    }
}

@Composable
fun DialogoNuevaEntradaConFuente(
    onCancelar: () -> Unit,
    onGuardar: (String, String, String) -> Unit
) {
    var fuenteSeleccionada by remember { mutableStateOf(SourceRegistry.todas.firstOrNull()) }
    var referenciaTexto by remember { mutableStateOf("") }
    var textoEntrada by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva entrada") },
        text = {
            Column {
                Text("Fuente:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceRegistry.todas.forEach { fuente ->
                        FilterChip(
                            selected = fuenteSeleccionada?.id == fuente.id,
                            onClick = { fuenteSeleccionada = fuente },
                            label = { Text(fuente.nombre) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = referenciaTexto,
                    onValueChange = { referenciaTexto = it },
                    label = { Text("Referencia (p. ej. Mateo 24:36)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = textoEntrada,
                    onValueChange = { textoEntrada = it },
                    label = { Text("Texto") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fuente = fuenteSeleccionada
                    if (fuente != null && referenciaTexto.isNotBlank() && textoEntrada.isNotBlank()) {
                        onGuardar(fuente.id, referenciaTexto, textoEntrada)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
