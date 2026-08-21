package com.renovacion.ep.sources

import com.renovacion.ep.core.CategoriaFuente
import com.renovacion.ep.core.Reference
import com.renovacion.ep.core.TextSource
import com.renovacion.ep.core.VerseResult

class WycliffeSource : TextSource {
    override val id = "wycliffe_1382"
    override val nombre = "John Wycliffe 1382"
    override val idioma = "Inglés medio"
    override val periodo = "1382 (primera traducción completa de la Biblia al inglés)"
    override val categoria = CategoriaFuente.TEXTO_INGLES_HISTORICO

    private val datosDePrueba: Map<String, String> = mapOf(
        "1 pedro 1:7" to "That the preuyng of youre feith myche more precious than gold is preued bi fier [texto de prueba]"
    )

    override fun entradasBase(): List<Pair<String, String>> =
        listOf("1 Pedro 1:7" to (datosDePrueba["1 pedro 1:7"] ?: ""))

    override fun buscar(referencia: Reference): VerseResult {
        val clave = referencia.display().lowercase()
        val texto = datosDePrueba[clave]
        return if (texto != null) {
            VerseResult(
                fuenteId = id,
                fuenteNombre = nombre,
                referencia = referencia.display(),
                texto = texto,
                disponible = true,
                nota = "Dato de prueba — pendiente de texto crítico definitivo."
            )
        } else {
            VerseResult(
                fuenteId = id,
                fuenteNombre = nombre,
                referencia = referencia.display(),
                texto = null,
                disponible = false,
                nota = "No disponible: agrega tus propias entradas con el botón +."
            )
        }
    }
}
