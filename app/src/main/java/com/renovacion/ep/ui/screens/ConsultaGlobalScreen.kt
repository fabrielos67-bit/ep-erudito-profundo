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

    fun buscarEnFuente(
        fuente: TextSource,
        referencia: Reference
    ): VerseResult? {

        if (
            EntradasStore.estaOculta(
                context,
                fuente.id,
                referencia.display()
            )
        ) {
            return null
        }

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
    }        /*
         * Una entrada ocultada en ESTE módulo no debe aparecer
         * en Consulta Global.
         */
        if (
                        resultados =
                SourceRegistry.todas
                    .filter {
                        it.id != "investigacion_personal"
                    }
                    .mapNotNull { fuente ->
                        buscarEnFuente(
                            fuente,
                            referencia
                        )
                    }

            buscado = true

        /*
         * Primero buscamos una entrada personalizada que
         * pertenezca exclusivamente a esta fuente.
         */
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

            resultados = fuentesConsulta
                .mapNotNull { fuente ->
                    buscarEnFuente(
                        fuente,
                        referencia
                    )
                }

            buscado = true
        }
    }

    fun abrirEntrada(
        fuenteId: String,
        referencia: String,
        texto: String
    ) {
        modoEdicion = ModoEdicionGlobal(
            fuenteId = fuenteId,
            referenciaInicial = referencia,
            textoInicial = texto,
            esNueva = false
        )
    }

    /*
     * Pantalla de edición de una entrada existente.
     */
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

                EntradasStore.guardar(
                    context,
                    edicionActual.fuenteId,
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
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    mostrarDialogo = true
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
                text = "Busca una referencia en las fuentes textuales.",
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

                Spacer(
                    modifier = Modifier.height(12.dp)
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

                if (resultados.isEmpty()) {

                    Text(
                        text = "No hay resultados disponibles.",
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
                            key = {
                                "${it.fuenteId}:${it.referencia}"
                            }
                        ) { resultado ->

                            ResultadoGlobalCard(
                                resultado = resultado,
                                onClick = {

                                    if (
                                        resultado.texto != null &&
                                        resultado.disponible
                                    ) {

                                        abrirEntrada(
                                            fuenteId = resultado.fuenteId,
                                            referencia = resultado.referencia,
                                            texto = resultado.texto
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

    if (mostrarDialogo) {

        DialogoNuevaEntradaConFuente(

            onCancelar = {
                mostrarDialogo = false
            },

            onGuardar = { fuenteId, referenciaTexto, textoEntrada ->

                EntradasStore.guardar(
                    context,
                    fuenteId,
                    referenciaTexto,
                    textoEntrada
                )

                mostrarDialogo = false

                if (consulta.isNotBlank()) {
                    ejecutarBusqueda()
                }
            }
        )
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

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = resultado.referencia,
                style = MaterialTheme.typography.titleLarge
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

            } else {

                Text(
                    text = "No disponible en ${resultado.fuenteNombre}.",
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
