package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.EntradasStore
import com.renovacion.ep.core.Reference
import com.renovacion.ep.core.ReferenceParser
import com.renovacion.ep.core.SourceRegistry
import com.renovacion.ep.core.TextSource
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
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var consulta by remember {
        mutableStateOf("")
    }

    var resultados by remember {
        mutableStateOf<List<VerseResult>>(emptyList())
    }

    var errorParsing by remember {
        mutableStateOf(false)
    }

    var buscado by remember {
        mutableStateOf(false)
    }

    var modoEdicion by remember {
        mutableStateOf<ModoEdicionGlobal?>(null)
    }

    fun buscarEnFuente(
        fuente: TextSource,
        referencia: Reference
    ): VerseResult {

        val textoGuardado =
            EntradasStore.obtenerTexto(
                context,
                fuente.id,
                referencia.display()
            )

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

        val referencia =
            ReferenceParser.parse(consulta)

        if (referencia == null) {

            errorParsing = true
            resultados = emptyList()
            buscado = false

        } else {

            errorParsing = false

            resultados =
                SourceRegistry.todas.map { fuente ->
                    buscarEnFuente(
                        fuente,
                        referencia
                    )
                }

            buscado = true
        }
    }

    val edicionActual = modoEdicion

    if (edicionActual != null) {

        EditorEntradaGlobal(
            fuenteId = edicionActual.fuenteId,
            referenciaInicial = edicionActual.referenciaInicial,
            textoInicial = edicionActual.textoInicial,
            esNueva = edicionActual.esNueva,

            onVolver = {
                modoEdicion = null
            },

            onGuardar = {
                    fuenteId,
                    referencia,
                    texto ->

                EntradasStore.guardar(
                    context,
                    fuenteId,
                    referencia,
                    texto
                )

                modoEdicion = null

                if (consulta.isNotBlank()) {
                    ejecutarBusqueda()
                }
            },

            onEliminar = {

                EntradasStore.eliminar(
                    context,
                    edicionActual.fuenteId,
                    edicionActual.referenciaInicial
                )

                modoEdicion = null

                if (consulta.isNotBlank()) {
                    ejecutarBusqueda()
                }
            }
        )

        return
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Consulta Global")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onVolver
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    val fuente =
                        SourceRegistry.todas.firstOrNull()

                    if (fuente != null) {

                        modoEdicion =
                            ModoEdicionGlobal(
                                fuenteId = fuente.id,
                                referenciaInicial = "",
                                textoInicial = "",
                                esNueva = true
                            )
                    }
                }

            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Agregar entrada"
                )
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
                text =
                    "Busca una referencia bíblica en todas las fuentes registradas.",

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(

                value = consulta,

                onValueChange = {
                    consulta = it
                    errorParsing = false
                },

                label = {
                    Text(
                        "Referencia (p. ej. Mateo 24:36)"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Button(
                onClick = {
                    ejecutarBusqueda()
                }
            ) {

                Text("Buscar")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            if (errorParsing) {

                Text(
                    text =
                        "No se pudo interpretar la referencia. " +
                                "Usa el formato \"Libro capítulo:verso\".",

                    color =
                        MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            if (buscado) {

                Text(
                    text =
                        "Resultados en ${resultados.size} fuente(s):",

                    style =
                        MaterialTheme.typography.titleLarge,

                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    resultados.forEach { resultado ->

                        ResultadoCardGlobal(
                            resultado = resultado,

                            onEditar = {

                                modoEdicion =
                                    ModoEdicionGlobal(

                                        fuenteId =
                                            resultado.fuenteId,

                                        referenciaInicial =
                                            resultado.referencia,

                                        textoInicial =
                                            resultado.texto ?: "",

                                        esNueva = false
                                    )
                            }
                        )
                    }
                }

            } else {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    Text(
                        text =
                            "Consulta Global permite buscar la misma " +
                                    "referencia en todas las fuentes.",

                        style =
                            MaterialTheme.typography.bodyLarge,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorEntradaGlobal(
    fuenteId: String,
    referenciaInicial: String,
    textoInicial: String,
    esNueva: Boolean,
    onVolver: () -> Unit,
    onGuardar: (String, String, String) -> Unit,
    onEliminar: () -> Unit
) {
    var referenciaTexto by remember {
        mutableStateOf(referenciaInicial)
    }

    var textoEntrada by remember {
        mutableStateOf(textoInicial)
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    val fuente =
        SourceRegistry.porId(fuenteId)

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        if (esNueva)
                            "Nueva entrada"
                        else
                            "Editar entrada"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onVolver
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },

                actions = {

                    IconButton(

                        onClick = {

                            if (
                                referenciaTexto.isNotBlank() &&
                                textoEntrada.isNotBlank()
                            ) {

                                onGuardar(
                                    fuenteId,
                                    referenciaTexto.trim(),
                                    textoEntrada
                                )
                            }
                        }

                    ) {

                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Guardar"
                        )
                    }

                    IconButton(

                        onClick = {
                            menuAbierto = true
                        }

                    ) {

                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }

                    DropdownMenu(

                        expanded = menuAbierto,

                        onDismissRequest = {
                            menuAbierto = false
                        }

                    ) {

                        DropdownMenuItem(

                            text = {
                                Text("Deshacer cambios")
                            },

                            onClick = {

                                referenciaTexto =
                                    referenciaInicial

                                textoEntrada =
                                    textoInicial

                                menuAbierto = false
                            }
                        )

                        if (!esNueva) {

                            DropdownMenuItem(

                                text = {
                                    Text("Eliminar")
                                },

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
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            Text(

                text =
                    fuente?.nombre
                        ?: "Fuente no encontrada",

                style =
                    MaterialTheme.typography.titleMedium,

                color =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(

                value = referenciaTexto,

                onValueChange = {
                    referenciaTexto = it
                },

                label = {
                    Text(
                        "Referencia (p. ej. Mateo 24:36)"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(

                value = textoEntrada,

                onValueChange = {
                    textoEntrada = it
                },

                label = {
                    Text("Texto")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                minLines = 12
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
private fun ResultadoCardGlobal(
    resultado: VerseResult,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = resultado.fuenteNombre,

                style =
                    MaterialTheme.typography.titleMedium,

                color =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = resultado.referencia,

                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (
                resultado.disponible &&
                resultado.texto != null
            ) {

                Text(
                    text = resultado.texto,

                    style =
                        MaterialTheme.typography.bodyLarge
                )

            } else {

                Text(
                    text =
                        "No disponible en ${resultado.fuenteNombre}.",

                    style =
                        MaterialTheme.typography.bodyLarge,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            resultado.nota?.let {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = it,

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextButton(
                onClick = onEditar
            ) {

                Text("Editar")
            }
        }
    }
}
