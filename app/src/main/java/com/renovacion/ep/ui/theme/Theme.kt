package com.renovacion.ep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EsquemaOscuro = darkColorScheme(
    primary = AcentoDorado,
    onPrimary = FondoOscuro,
    secondary = AcentoDoradoSuave,
    onSecondary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoPrincipal,
    surface = SuperficieOscura,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieOscuraAlt,
    onSurfaceVariant = TextoSecundario,
    error = Error
)

@Composable
fun EPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = Tipografia,
        content = content
    )
}
