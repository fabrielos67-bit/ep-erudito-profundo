package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.EntradasStore
import com.renovacion.ep.core.ReferenceParser
import com.renovacion.ep.core.SourceRegistry
import com.renovacion.ep.core.VerseResult

private data class ModoEdicionGlobal(
    val fuenteId: String,
    val referenciaInicial: String,
    val textoInicial: String,
    val esNueva: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaGlobalScreen(
    onVolver: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<VerseResult>>(emptyList()) }
    var errorParsing by remember { mutableStateOf(false) }
    var modoEdicion by remember { mutableStateOf<ModoEdicionGlobal?>(null) }

    val fuentesConsulta = SourceRegistry.todas
    val ID_FUENTE_GLOBAL = "global_user_entries"

    fun cargarEntradas() {
        val listaResultados = mutableListOf<VerseResult>()

        val refABuscar = consulta.trim()
        val textoGuardado = if (refABuscar.isNotBlank()) {
            EntradasStore.obtenerTexto(context, ID_FUENTE_GLOBAL, refABuscar)
        } else null

        if (textoGuardado != null) {
            listaResultados.add(
                VerseResult(
                    fuenteId = ID_FUENTE_GLOBAL,
                    fuenteNombre = "Mi Entrada Personal",
                    referencia = refABuscar,
                    texto = textoGuardado,
                    disponible = true,
                    nota = "Entrada agregada por ti."
                )
            )
        }

        if (refABuscar.isNotBlank()) {
            val referencia = ReferenceParser.parse(refABuscar)
            if (referencia == null) {
                errorParsing = true
            } else {
                errorParsing = false
                fuentesConsulta.forEach { fuente ->
                    val txt = EntradasStore.obtenerTexto(context, fuente.id, referencia.display())
                    if (txt != null) {
                        listaResultados.add(
                            VerseResult(
                                fuenteId = fuente.id,
                                fuenteNombre = fuente.nombre,
                                referencia = referencia.display(),
                                texto = txt,
                                disponible = true,
                                nota = "Entrada agregada por ti."
                            )
                        )
                    } else {
                        val res = fuente.buscar(referencia)
                        if (res != null && res.disponible && !res.texto.isNullOrBlank()) {
                            listaResultados.add(res)
                        }
                    }
                }
            }
        } else {
            errorParsing = false
        }

        resultados = listaResultados
    }

    LaunchedEffect(Unit) {
        cargarEntradas()
    }

    fun abrirEdicion(
        fuenteId: String,
        referencia: String,
        texto: String,
        esNueva: Boolean
    ) {
        modoEdicion = ModoEdicionGlobal(
            fuenteId = fuenteId,
            referenciaInicial = referencia,
            textoInicial = texto,
            esNueva = esNueva
        )
    }

    val edicionActual = modoEdicion

    if (edicionActual != null) {
        PantallaEdicionGlobal(
            referenciaInicial = edicionActual.referenciaInicial,
            textoInicial = edicionActual.textoInicial,
            permiteEliminar = !edicionActual.esNueva,
            onVolver = {
                modoEdicion = null
            },
            onGuardar = { referenciaTexto, textoEntrada ->
                EntradasStore.guardar(context, edicionActual.fuenteId, referenciaTexto, textoEntrada)
                modoEdicion = null
                consulta = referenciaTexto
                cargarEntradas()
            },
            onEliminar = {
                EntradasStore.eliminar(context, edicionActual.fuenteId, edicionActual.referenciaInicial)
                modoEdicion = null
                cargarEntradas()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta Global") },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    } else {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    abrirEdicion(
                        fuenteId = ID_FUENTE_GLOBAL,
                        referencia = consulta,
                        texto = "",
                        esNueva = true
                    )
                }
            ) {
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
                text = "Busca una referencia en las fuentes textuales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = consulta,
                onValueChange = {
                    consulta = it
                    errorParsing = false
                },
                label = { Text("Referencia (p. ej. Mateo 24:36)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { cargarEntradas() }) {
                Text("Buscar")
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (errorParsing) {
                Text(
                    text = "No se pudo interpretar la referencia. Usa el formato \"Libro capítulo:verso\".",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Resultados (${resultados.size}):",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (resultados.isEmpty()) {
                Text(
                    text = "No hay entradas disponibles para esta consulta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = resultados,
                        key = { "${it.fuenteId}:${it.referencia}" }
                    ) { resultado ->
                        ResultadoGlobalCard(
                            resultado = resultado,
                            onClick = {
                                if (resultado.texto != null && resultado.disponible) {
                                    abrirEdicion(
                                        fuenteId = resultado.fuenteId,
                                        referencia = resultado.referencia,
                                        texto = resultado.texto,
                                        esNueva = false
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultadoGlobalCard(
    resultado: VerseResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = resultado.fuenteNombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resultado.referencia,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resultado.texto ?: "",
                style = MaterialTheme.typography.bodyLarge
            )

            resultado.nota?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEdicionGlobal(
    referenciaInicial: String,
    textoInicial: String,
    permiteEliminar: Boolean,
    onVolver: () -> Unit,
    onGuardar: (referenciaTexto: String, textoEntrada: String) -> Unit,
    onEliminar: () -> Unit
) {
    var referenciaTexto by remember { mutableStateOf(referenciaInicial) }
    var textoEntrada by remember { mutableStateOf(textoInicial) }
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (permiteEliminar) "Editar entrada" else "Nueva entrada"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (referenciaTexto.isNotBlank() && textoEntrada.isNotBlank()) {
                                onGuardar(referenciaTexto.trim(), textoEntrada)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Guardar")
                    }

                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                    }

                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Guardar") },
                            onClick = {
                                menuAbierto = false
                                if (referenciaTexto.isNotBlank() && textoEntrada.isNotBlank()) {
                                    onGuardar(referenciaTexto.trim(), textoEntrada)
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Deshacer cambios") },
                            onClick = {
                                referenciaTexto = referenciaInicial
                                textoEntrada = textoInicial
                                menuAbierto = false
                            }
                        )

                        if (permiteEliminar) {
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = {
                                    menuAbierto = false
                                    onEliminar()
                                }
                            )
                        }
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
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = referenciaTexto,
                onValueChange = { referenciaTexto = it },
                label = { Text("Referencia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = textoEntrada,
                onValueChange = { textoEntrada = it },
                label = { Text("Texto") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onVolver) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (referenciaTexto.isNotBlank() && textoEntrada.isNotBlank()) {
                            onGuardar(referenciaTexto.trim(), textoEntrada)
                        }
                    }
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
