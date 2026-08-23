package com.renovacion.ep.ui.screens

import androidx.compose.foundation.clickable
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

private data class EntradaGlobal(
    val fuenteId: String,
    val fuenteNombre: String,
    val referencia: String,
    val texto: String
)

private data class ModoEdicionGlobal(
    val fuenteId: String,
    val fuenteNombre: String,
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

    var entradasGlobales by remember {
        mutableStateOf<List<EntradaGlobal>>(emptyList())
    }

    fun recargarEntradas() {
        val lista = mutableListOf<EntradaGlobal>()

        SourceRegistry.todas.forEach { fuente ->

            val entradas = EntradasStore.obtenerTodas(
                context,
                fuente.id
            )

            entradas.forEach { entrada ->

                lista.add(
                    EntradaGlobal(
                        fuenteId = fuente.id,
                        fuenteNombre = fuente.nombre,
                        referencia = entrada.first,
                        texto = entrada.second
                    )
                )
            }
        }

        entradasGlobales = lista.sortedWith(
            compareBy(
                { it.fuenteNombre.lowercase() },
                { it.referencia.lowercase() }
            )
        )
    }

    fun buscarEnFuente(
        fuente: TextSource,
        referencia: Reference
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

        val referencia = ReferenceParser.parse(
            consulta
        )

        if (referencia == null) {

            errorParsing = true
            resultados = emptyList()
            buscado = false

        } else {

            errorParsing = false

            resultados = SourceRegistry.todas.map { fuente ->
                buscarEnFuente(
                    fuente,
                    referencia
                )
            }

            buscado = true
        }
    }

    LaunchedEffect(Unit) {
        recargarEntradas()
    }

    val edicionActual = modoEdicion

    if (edicionActual != null) {

        PantallaEdicionGlobal(
            fuenteNombre = edicionActual.fuenteNombre,
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

                recargarEntradas()

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

                recargarEntradas()

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

                    val primeraFuente =
                        SourceRegistry.todas.firstOrNull()

                    if (primeraFuente != null) {

                        modoEdicion =
                            ModoEdicionGlobal(
                                fuenteId = primeraFuente.id,
                                fuenteNombre = primeraFuente.nombre,
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
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Consulta Global",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Busca una referencia en todas las fuentes registradas.",
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
                    Text(
                        "Referencia (p. ej. Mateo 24:36)"
                    )
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Button(

                onClick = {
                    ejecutarBusqueda()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Buscar")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
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
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                LazyColumn(

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),

                    contentPadding =
                        PaddingValues(
                            bottom = 100.dp
                        )
                ) {

                    items(
                        items = resultados,
                        key =
