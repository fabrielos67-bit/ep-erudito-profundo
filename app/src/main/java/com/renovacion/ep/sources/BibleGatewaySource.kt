package com.renovacion.ep.sources

import com.renovacion.ep.core.CategoriaFuente
import com.renovacion.ep.core.Reference
import com.renovacion.ep.core.TextSource
import com.renovacion.ep.core.VerseResult

class BibleGatewaySource : TextSource {
    override val id = "bible_gateway"
    override val nombre = "Bible Gateway (English Translations)"
override val idioma = "Inglés traducciones modernas en línea."
override val periodo = ""
    override val categoria = CategoriaFuente.FUENTE_EXTERNA

    private val datosDePrueba: Map<String, String> = emptyMap()

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
