package com.renovacion.ep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.renovacion.ep.core.SourceRegistry

private data class Modulo(
    val titulo: String,
    val subtitulo: String,
    val ruta: String
)

@Composable
fun HomeScreen(
    onAbrirModulo: (String) -> Unit,
    onAbrirConsultaGlobal: () -> Unit
) {
    val modulos = SourceRegistry.todas.map {
        Modulo(titulo = it.nombre, subtitulo = "${it.idioma} · ${it.periodo}", ruta = it.id)
    }

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
            items(modulos) { modulo ->
                TarjetaModulo(
                    titulo = modulo.titulo,
                    subtitulo = modulo.subtitulo,
                    onClick = { onAbrirModulo(modulo.ruta) }
                )
            }
            item {
                TarjetaModulo(
                    titulo = "Consulta Global",
                    subtitulo = "Buscar una referencia en todas las fuentes registradas",
                    onClick = onAbrirConsultaGlobal
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaModulo(titulo: String, subtitulo: String, onClick: () -> Unit) {
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
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
