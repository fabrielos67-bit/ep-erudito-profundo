package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun ConsultaGlobalScreen(
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var consulta by remember { mutableStateOf("") }
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
        mutableStateOf<ModoEdicion?>(null)
    }

    var mostrarSelectorFuente by remember {
        mutableStateOf(false)
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

    /*
     * IMPORTANTE:
     * Consulta Global utiliza exactamente el mismo editor
     * que utilizan los demás módulos.
     */
    if (edicionActual != null) {

        val fuente = SourceRegistry.porId(
            edicionActual.fuenteId
        )

        if (fuente != null) {

            PantallaEdicionEntrada(

                referenciaInicial =
                    edicionActual.referenciaInicial,

                textoInicial =
                    edicionActual.textoInicial,

                permiteEliminar =
                    !edicionActual.esNueva,

                onVolver = {
                    modoEdicion = null
                },

                onGuardar = {
                    referenciaTexto,
                    textoEntrada ->

                    EntradasStore.guardar(
                        context,
                        fuente.id,
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
                        fuente.id,
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
                    mostrarSelectorFuente = true
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
                    Text(
                        "Referencia (p. ej. Mateo 24:36)"
                    )
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
                    text =
                        "No se pudo interpretar la referencia. " +
                        "Usa el formato \"Libro capítulo:verso\".",

                    color =
                        MaterialTheme.colorScheme.error
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
                    modifier = Modifier.height(10.dp)
                )

                resultados.forEach { resultado ->

                    ResultadoCard(resultado)

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

    /*
     * Selector de fuente solamente para una NUEVA entrada.
     * Después de seleccionar la fuente se abre el mismo editor
     * normal de los módulos.
     */
    if (mostrarSelectorFuente) {

        AlertDialog(

            onDismissRequest = {
                mostrarSelectorFuente = false
            },

            title = {
                Text("Seleccionar fuente")
            },

            text = {

                Column {

                    SourceRegistry.todas.forEach { fuente ->

                        TextButton(

                            onClick = {

                                mostrarSelectorFuente = false

                                modoEdicion =
                                    ModoEdicion(

                                        referenciaInicial =
                                            consulta,

                                        textoInicial = "",

                                        esNueva = true
                                    )

                                fuenteSeleccionadaTemporal =
                                    fuente.id
                            },

                            modifier =
                                Modifier.fillMaxWidth()

                        ) {

                            Text(
                                text = fuente.nombre
                            )
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        mostrarSelectorFuente = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
