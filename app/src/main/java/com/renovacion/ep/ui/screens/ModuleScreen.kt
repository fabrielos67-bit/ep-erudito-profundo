package com.renovacion.ep.ui.screens

import androidx.compose.foundation.clickable
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
import com.renovacion.ep.core.ReferenceParser
import com.renovacion.ep.core.SourceRegistry
import com.renovacion.ep.core.VerseResult

private data class ModoEdicion(
    val referenciaInicial: String,
    val textoInicial: String,
    val esNueva: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(
    sourceId: String,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val fuente = SourceRegistry.porId(sourceId)

    var consulta by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<VerseResult?>(null) }
    var errorParsing by remember { mutableStateOf(false) }
    var entradasGuardadas by remember {
        mutableStateOf(listOf<Pair<String, String>>())
    }
    var modoEdicion by remember { mutableStateOf<ModoEdicion?>(null) }

    fun recargarEntradas() {
        if (fuente != null) {
            val propias = EntradasStore.obtenerTodas(context, fuente.id)

            val clavesPropias = propias
                .map { it.first.trim().lowercase() }
                .toSet()

            val base = fuente.entradasBase().filter { (referencia, _) ->
                val clave = referencia.trim().lowercase()

                clave !in clavesPropias &&
                    !EntradasStore.estaOculta(
                        context,
                        fuente.id,
                        referencia
                    )
            }

            entradasGuardadas =
                (base + propias)
                    .sortedBy { it.first.lowercase() }
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

            val textoGuardado = EntradasStore.obtenerTexto(
                context,
                fuente.id,
                referencia.display()
            )

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

    val edicionActual = modoEdicion

    if (edicionActual != null && fuente != null) {

        PantallaEdicionEntrada(
            referenciaInicial = edicionActual.referenciaInicial,
            textoInicial = edicionActual.textoInicial,
            permiteEliminar = !edicionActual.esNueva,

            onVolver = {
                modoEdicion = null
            },

            onGuardar = { referenciaTexto, textoEntrada ->

                EntradasStore.guardar(
                    context,
                    fuente.id,
                    referenciaTexto,
                    textoEntrada
                )

                modoEdicion = null
                recargarEntradas()

                if (consulta.isNotBlank()) {
                    buscarConEntradas()
                }
            },

            onEliminar = {

                EntradasStore.eliminar(
                    context,
                    fuente.id,
                    edicionActual.referenciaInicial
                )

                modoEdicion = null
                recargarEntradas()

                if (consulta.isNotBlank()) {
                    buscarConEntradas()
                }
            }
        )

        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(fuente?.nombre ?: "Módulo")
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

            if (fuente != null) {

                FloatingActionButton(
                    onClick = {
                        modoEdicion = ModoEdicion(
                            referenciaInicial = "",
                            textoInicial = "",
                            esNueva = true
                        )
                    }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Agregar entrada"
                    )
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

            Spacer(
                modifier = Modifier.height(20.dp)
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
                    buscarConEntradas()
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

            resultado?.let {

                ResultadoCard(it)

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            if (entradasGuardadas.isNotEmpty()) {

                Text(
                    text = "Entradas guardadas (${entradasGuardadas.size}) — toca una para abrirla:",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Column {

                    entradasGuardadas.forEach { entrada ->

                        FilaEntrada(
                            referencia = entrada.first,

                            onClick = {

                                modoEdicion = ModoEdicion(
                                    referenciaInicial = entrada.first,
                                    textoInicial = entrada.second,
                                    esNueva = false
                                )
                            }
                        )

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaEntrada(
    referencia: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(vertical = 16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = referencia,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEdicionEntrada(
    referenciaInicial: String,
    textoInicial: String,
    permiteEliminar: Boolean,
    onVolver: () -> Unit,
    onGuardar: (String, String) -> Unit,
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

@Composable
fun ResultadoCard(
    resultado: VerseResult
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

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
                    resultado.texto,
                    style = MaterialTheme.typography.bodyLarge
                )

            } else {

                Text(
                    "No disponible en ${resultado.fuenteNombre}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            resultado.nota?.let {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
