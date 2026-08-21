package com.renovacion.ep.sources

import com.renovacion.ep.core.CategoriaFuente
import com.renovacion.ep.core.Reference
import com.renovacion.ep.core.TextSource
import com.renovacion.ep.core.VerseResult

class WessexSource : TextSource {
    override val id = "wessex_1175"
    override val nombre = "Wessex c.1175"
    override val idioma = "Inglés antiguo (sajón occidental)"
    override val periodo = "c. 1175 (testimonio manuscrito de los Evangelios de Wessex)"
    override val categoria = CategoriaFuente.TEXTO_INGLES_HISTORICO

    private val datosDePrueba: Map<String, String> = mapOf(
        "mateo 24:36" to "Be þam dæge soðlice & be þære tide nat nan man [texto de prueba]"
    )

    override fun entradasBase(): List<Pair<String, String>> =
        listOf("Mateo 24:36" to (datosDePrueba["mateo 24:36"] ?: ""))

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
                nota = "No disponible: esta fuente cubre únicamente los cuatro Evangelios."
            )
        }
    }
}
