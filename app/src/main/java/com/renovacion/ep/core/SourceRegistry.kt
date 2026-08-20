package com.renovacion.ep.core

import com.renovacion.ep.sources.BibleGatewaySource
import com.renovacion.ep.sources.InvestigacionPersonalSource
import com.renovacion.ep.sources.MasoreticoSource
import com.renovacion.ep.sources.WessexSource
import com.renovacion.ep.sources.WycliffeSource

object SourceRegistry {

    private val masoretico1524 = MasoreticoSource()
    private val wessex1175 = WessexSource()
    private val wycliffe1382 = WycliffeSource()
    private val bibleGateway = BibleGatewaySource()
    private val investigacionPersonal = InvestigacionPersonalSource()

    val todas: List<TextSource> = listOf(
        masoretico1524,
        wessex1175,
        wycliffe1382,
        bibleGateway,
        investigacionPersonal
        // --- Preparado para futuras incorporaciones ---
        // GriegoSource(),
        // HebreoSource(),
        // AramaicoSiriacoSource(),
        // InglesSource(),
        // EspanolSource(),
        // LexicoSource(),
    )

    fun porId(id: String): TextSource? = todas.find { it.id == id }
}
