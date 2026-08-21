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
import com.renovacion.ep.core.VerseResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(sourceId: String, onVolver: () -> Unit) {
    val context = LocalContext.current
    val fuente = SourceRegistry.porId(sourceId)
    var consulta by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<VerseResult?>(null) }
    var errorParsing by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var entradasGuardadas by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var entradaEditando by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun recargarEntradas() {
        if (fuente != null) {
            entradasGuardadas = EntradasStore.obtenerTodas(context, fuente.id)
        }
    }

    LaunchedEffect(sourceId) {
        recargarEntradas()
    }

    fun buscarConEntradas() {
        val referencia = ReferenceParser.parse(consulta)
        if (referencia == null) {
            errorParsing = true
            resultado = null
        } else if (fuente != null) {
            val textoGuardado = EntradasStore.obtenerTexto(context, fuente.id, referencia.display())
            resultado = if (textoGuardado != null) {
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
    }

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
        },
        floatingActionButton = {
            if (fuente != null) {
                FloatingActionButton(onClick = { mostrarDialogo = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar entrada")
                }
            }
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
                Button(onClick = { buscarConEntradas() }) {
                    Text("Buscar")
                }
                if (fuente.id == "wessex_1175") {
                    OutlinedButton(onClick = { consulta = "Mateo 24:36" }) {
                        Text("Mateo 24:36")
                    }
                    OutlinedButton(onClick = { consulta = "1 Pedro 1:7" }) {
                        Text("1 Pedro 1:7")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (errorParsing) {
                Text(
                    "No se pudo interpretar la referencia. Usa el formato \"Libro capítulo:verso\".",
                    color = MaterialTheme.colorScheme.error
                )
            }

            resultado?.let {
                ResultadoCard(it)
                Spacer(Modifier.height(20.dp))
            }

            if (entradasGuardadas.isNotEmpty()) {
                Text(
                    "Tus entradas guardadas (${entradasGuardadas.size}) — toca una para editarla:",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entradasGuardadas) { entrada ->
                        EntradaGuardadaCard(
                            referencia = entrada.first,
                            texto = entrada.second,
                            onClick = { entradaEditando = entrada }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo && fuente != null) {
        DialogoEntrada(
            referenciaInicial = "",
            textoInicial = "",
            permiteEliminar = false,
            onCancelar = { mostrarDialogo = false },
            onGuardar = { referenciaTexto, textoEntrada ->
                EntradasStore.guardar(context, fuente.id, referenciaTexto, textoEntrada)
                mostrarDialogo = false
                recargarEntradas()
                if (consulta.isNotBlank()) {
                    buscarConEntradas()
                }
            },
            onEliminar = {}
        )
    }

    entradaEditando?.let { (referenciaActual, textoActual) ->
        if (fuente != null) {
            DialogoEntrada(
                referenciaInicial = referenciaActual,
                textoInicial = textoActual,
                permiteEliminar = true,
                onCancelar = { entradaEditando = null },
                onGuardar = { referenciaTexto, textoEntrada ->
                    EntradasStore.guardar(context, fuente.id, referenciaTexto, textoEntrada)
                    entradaEditando = null
                    recargarEntradas()
                    if (consulta.isNotBlank()) {
                        buscarConEntradas()
                    }
                },
                onEliminar = {
                    EntradasStore.eliminar(context, fuente.id, referenciaActual)
                    entradaEditando = null
                    recargarEntradas()
                    if (consulta.isNotBlank()) {
                        buscarConEntradas()
                    }
                }
            )
        }
    }
}

@Composable
fun DialogoEntrada(
    referenciaInicial: String,
    textoInicial: String,
    permiteEliminar: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (String, String) -> Unit,
    onEliminar: () -> Unit
) {
    var referenciaTexto by remember { mutableStateOf(referenciaInicial) }
    var textoEntrada by remember { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (permiteEliminar) "Editar entrada" else "Nueva entrada") },
        text = {
            Column {
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
                if (permiteEliminar) {
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onEliminar) {
                        Text("Eliminar esta entrada", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (referenciaTexto.isNotBlank() && textoEntrada.isNotBlank()) {
                        onGuardar(referenciaTexto, textoEntrada)
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

@Composable
fun EntradaGuardadaCard(referencia: String, texto: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = referencia,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Toca para editar o eliminar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
