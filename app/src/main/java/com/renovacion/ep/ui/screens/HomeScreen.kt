package com.renovacion.ep.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.SourceRegistry

private data class Modulo(
    val titulo: String,
    val subtitulo: String,
    val ruta: String,
    val enlace: String? = null
)

private data class RecursoExterno(
    val titulo: String,
    val subtitulo: String,
    val enlace: String
)

private val enlacesPorFuente = mapOf(
    "masoretico_1524" to "https://www.textusreceptusbibles.com/Masoretic",
    "wessex_1175" to "https://www.textusreceptusbibles.com/Wessex",
    "wycliffe_1382" to "https://www.textusreceptusbibles.com/Wycliffe",
    "investigacion_personal" to "https://logosklogos.com/"
)

private const val ENLACE_CONSULTA_GLOBAL = "https://www.biblegateway.com/"

private val recursosExternos = listOf(
    RecursoExterno(
        titulo = "Google Traductor",
        subtitulo = "Consultar o traducir palabras y textos",
        enlace = "https://translate.google.com/"
    ),
    RecursoExterno(
        titulo = "Biblia Paralela",
        subtitulo = "Comparar versiones lado a lado",
        enlace = "https://bibliaparalela.com/"
    ),
    RecursoExterno(
        titulo = "Diccionario Bíblico",
        subtitulo = "Consultar términos y definiciones",
        enlace = "https://www.google.com/amp/s/www.bibliatodo.com/amp/Diccionario-biblico"
    ),
    RecursoExterno(
        titulo = "Concordancia Bíblica",
        subtitulo = "Buscar palabras y sus apariciones",
        enlace = "https://www.bibliatodo.com/concordancia-biblica"
    )
)

@Composable
fun HomeScreen(
    onAbrirModulo: (String) -> Unit,
    onAbrirConsultaGlobal: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val todosLosModulos = SourceRegistry.todas.map {
        Modulo(
            titulo = it.nombre,
            subtitulo = "${it.idioma} · ${it.periodo}",
            ruta = it.id,
            enlace = enlacesPorFuente[it.id]
        )
    }
    val investigacionPersonal = todosLosModulos.find { it.ruta == "investigacion_personal" }
    val modulosPrincipales = todosLosModulos.filterNot { it.ruta == "investigacion_personal" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "EP — Erudito Profundo",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Proyecto Renovación",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        Text(
            text = "Módulos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(modulosPrincipales) { modulo ->
                TarjetaModulo(
                    titulo = modulo.titulo,
                    subtitulo = modulo.subtitulo,
                    enlace = modulo.enlace,
                    onClick = { onAbrirModulo(modulo.ruta) }
                )
            }
            item {
                TarjetaModulo(
                    titulo = "Consulta Global",
                    subtitulo = "Bible Gateway (English Translations) — Inglés, traducciones modernas en línea.",
                    enlace = ENLACE_CONSULTA_GLOBAL,
                    onClick = onAbrirConsultaGlobal
                )
            }
            investigacionPersonal?.let { modulo ->
                item {
                    TarjetaModulo(
                        titulo = modulo.titulo,
                        subtitulo = modulo.subtitulo,
                        enlace = modulo.enlace,
                        onClick = { onAbrirModulo(modulo.ruta) }
                    )
                }
            }
            items(recursosExternos) { recurso ->
                TarjetaModulo(
                    titulo = recurso.titulo,
                    subtitulo = recurso.subtitulo,
                    enlace = null,
                    onClick = { uriHandler.openUri(recurso.enlace) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaModulo(
    titulo: String,
    subtitulo: String,
    enlace: String?,
    onClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            if (enlace != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri(enlace) }
                )
            } else {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
