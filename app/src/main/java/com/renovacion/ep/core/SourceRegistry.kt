package com.renovacion.ep.core

import com.renovacion.ep.sources.MasoreticoSource
import com.renovacion.ep.sources.WessexSource

object SourceRegistry {

    private val masoretico1524 = MasoreticoSource()
    private val wessex1175 = WessexSource()

    val todas: List<TextSource> = listOf(
        masoretico1524,
        wessex1175
        // --- Preparado para futuras incorporaciones ---
        // GriegoSource(),
        // HebreoSource(),
        // AramaicoSiriacoSource(),
        // InglesSource(),
        // EspanolSource(),
        // LexicoSource(),
        // FuenteExternaSource(),
    )

    fun porId(id: String): TextSource? = todas.find { it.id == id }
}
