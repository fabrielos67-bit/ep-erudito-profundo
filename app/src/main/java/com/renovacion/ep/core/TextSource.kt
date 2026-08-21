package com.renovacion.ep.core

enum class CategoriaFuente {
    TEXTO_HEBREO,
    TEXTO_GRIEGO,
    TEXTO_ARAMEO_SIRIACO,
    TEXTO_INGLES_HISTORICO,
    TEXTO_ESPANOL,
    LEXICO,
    FUENTE_EXTERNA
}

data class VerseResult(
    val fuenteId: String,
    val fuenteNombre: String,
    val referencia: String,
    val texto: String?,
    val disponible: Boolean,
    val nota: String? = null
)

interface TextSource {
    val id: String
    val nombre: String
    val idioma: String
    val periodo: String
    val categoria: CategoriaFuente

    fun buscar(referencia: Reference): VerseResult

    /** Entradas de prueba propias de esta fuente (referencia a texto). */
    fun entradasBase(): List<Pair<String, String>> = emptyList()
}
