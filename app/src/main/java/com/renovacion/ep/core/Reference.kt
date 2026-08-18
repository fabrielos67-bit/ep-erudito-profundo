package com.renovacion.ep.core

data class Reference(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val verseEnd: Int? = null
) {
    fun display(): String {
        val versos = if (verseEnd != null && verseEnd != verse) "$verse-$verseEnd" else "$verse"
        return "$book $chapter:$versos"
    }
}

object ReferenceParser {

    private val PATRON = Regex(
        """^\s*((?:\d\s+)?[\p{L}.]+(?:\s+[\p{L}.]+)*)\s+(\d+)[:.](\d+)(?:-(\d+))?\s*$"""
    )

    fun parse(input: String): Reference? {
        val coincidencia = PATRON.find(input) ?: return null
        val (libroRaw, capituloRaw, versoRaw, versoFinRaw) = coincidencia.destructured
        val libro = normalizarLibro(libroRaw.trim())
        val capitulo = capituloRaw.toIntOrNull() ?: return null
        val verso = versoRaw.toIntOrNull() ?: return null
        val versoFin = versoFinRaw.toIntOrNull()
        return Reference(libro, capitulo, verso, versoFin)
    }

    private fun normalizarLibro(libro: String): String {
        val limpio = libro.trim().replace(Regex("\\s+"), " ")
        val clave = limpio.lowercase()
        return ALIAS[clave] ?: limpio
    }

    private val ALIAS: Map<String, String> = mapOf(
        "mt" to "Mateo",
        "mateo" to "Mateo",
        "1p" to "1 Pedro",
        "1 pedro" to "1 Pedro",
        "1pe" to "1 Pedro",
        "i pedro" to "1 Pedro"
    )
}
