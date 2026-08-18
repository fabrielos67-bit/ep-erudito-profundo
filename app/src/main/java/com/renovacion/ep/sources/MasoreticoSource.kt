package com.renovacion.ep.sources

import com.renovacion.ep.core.CategoriaFuente
import com.renovacion.ep.core.Reference
import com.renovacion.ep.core.TextSource
import com.renovacion.ep.core.VerseResult

class MasoreticoSource : TextSource {
    override val id = "masoretico_1524"
    override val nombre = "Masorético 1524"
    override val idioma = "Hebreo"
    override val periodo = "1524-25 (Biblia Rabínica de Bomberg)"
    override val categoria = CategoriaFuente.TEXTO_HEBREO

    private val datosDePrueba: Map<String, String> = mapOf(
        "génesis 1:1" to "בְּרֵאשִׁית בָּרָא אֱלֹהִים אֵת הַשָּׁמַיִם וְאֵת הָאָרֶץ׃ [texto de prueba]"
    )

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
                nota = "No disponible: esta fuente cubre el Antiguo Testamento hebreo."
            )
        }
    }
}
