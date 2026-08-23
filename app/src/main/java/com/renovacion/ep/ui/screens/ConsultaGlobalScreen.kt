package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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

    var consulta by remember { mutableStateOf("") }
    var resultados by remember {
        mutableStateOf<List<VerseResult>>(emptyList())
    }
    var errorParsing by remember { mutableStateOf(false) }
    var buscado by remember { mutableStateOf(false) }

    var modoEdicion by remember {
        mutableStateOf<ModoEdicionGlobal?>(null)
    }

    fun buscarEnFuente(
        fuente: TextSource,
        referencia: com.renovacion.ep.core.Reference
    ): VerseResult {

        val textoGuardado = EntradasStore.obtenerTexto(
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

        val referencia = ReferenceParser.parse(consulta)

        if (referencia == null) {

            errorParsing = true
            resultados = emptyList()
            buscado = false

        } else {

            errorParsing = false

            resultados = SourceRegistry.todas
                .map {
                    buscarEnFuente(it, referencia)
                }
                .filter {
                    it.disponible
                }

            buscado = true
        }
    }

    val edicionActual = modoEdicion

    if (edicionActual != null) {

        PantallaEdicionGlobal(
            fuenteInicial = edicionActual.fuenteId,
            referenciaInicial = edicionActual.referenciaInicial,
            textoInicial = edicionActual.textoInicial,
            permiteEliminar = !edicionActual.esNueva,

            onVolver = {
                modoEdicion = null
            },

            onGuardar = { fuenteId, referenciaTexto, textoEntrada ->

                EntradasStore.guardar(
                    context,
                    fuenteId,
                    referenciaTexto,
                    textoEntrada
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
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            Text(
                text = "Busca una referencia bíblica en todas las fuentes registradas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text("Referencia (p. ej. Mateo 24:36)")
                },

                modifier = Modifier.fillMaxWidth()
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
                    text = "No se pudo interpretar la referencia. Usa el formato \"Libro capítulo:verso\".",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (buscado) {

                Text(
                    text = "Resultados en ${resultados.size} fuente(s):",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                resultados.forEach { resultado ->

                    ResultadoCardGlobal(
                        resultado = resultado,
                        onEditar = {

                            modoEdicion = ModoEdicionGlobal(
                                fuenteId = resultado.fuenteId,
                                referenciaInicial = resultado.referencia,
                                textoInicial = resultado.texto ?: "",
                                esNueva = false
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(80.dp)
            )
        }
    }

    FloatingActionButton(
        onClick = {

            val fuenteInicial =
                SourceRegistry.todas.firstOrNull()

            if (fuenteInicial != null) {

                modoEdicion = ModoEdicionGlobal(
                    fuenteId = fuenteInicial.id,
                    referenciaInicial = consulta,
                    textoInicial = "",
                    esNueva = true
                )
            }
        },

        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = "Nueva entrada"
        )
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = resultado.referencia,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
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
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                TextButton(
                    onClick = onEditar
                ) {
                    Text("Editar")
                }

            } else {

                Text(
                    text = "No disponible.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            resultado.nota?.let {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

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
    fuenteInicial: String,
    referenciaInicial: String,
    textoInicial: String,
    permiteEliminar: Boolean,
    onVolver: () -> Unit,
    onGuardar: (String, String, String) -> Unit,
    onEliminar: () -> Unit
) {

    var fuenteId by remember {
        mutableStateOf(fuenteInicial)
    }

    var referenciaTexto by remember {
        mutableStateOf(referenciaInicial)
    }

    var textoEntrada by remember {
        mutableStateOf(textoInicial)
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    var fuenteMenuAbierto by remember {
        mutableStateOf(false)
    }

    val fuenteSeleccionada =
        SourceRegistry.todas.firstOrNull {
            it.id == fuenteId
        }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        if (permiteEliminar)
                            "Editar entrada"
                        else
                            "Nueva entrada"
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
                                    referenciaTexto,
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

                                fuenteId =
                                    fuenteInicial

                                menuAbierto = false
                            }
                        )

                        if (permiteEliminar) {

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
                text = "Fuente",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedButton(

                onClick = {
                    fuenteMenuAbierto = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    fuenteSeleccionada?.nombre
                        ?: "Seleccionar fuente"
                )
            }

            DropdownMenu(

                expanded = fuenteMenuAbierto,

                onDismissRequest = {
                    fuenteMenuAbierto = false
                }

            ) {

                SourceRegistry.todas.forEach { fuente ->

                    DropdownMenuItem(

                        text = {
                            Text(fuente.nombre)
                        },

                        onClick = {

                            fuenteId = fuente.id
                            fuenteMenuAbierto = false
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(

                value = referenciaTexto,

                onValueChange = {
                    referenciaTexto = it
                },

                label = {
                    Text("Referencia (p. ej. Mateo 24:36)")
                },

                modifier = Modifier.fillMaxWidth()
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

                modifier = Modifier.fillMaxWidth(),

                minLines = 10
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}
