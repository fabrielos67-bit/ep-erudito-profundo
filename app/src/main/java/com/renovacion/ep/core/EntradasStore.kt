package com.renovacion.ep.core

import android.content.Context
import androidx.core.content.edit

object EntradasStore {
    private const val PREFS_NAME = "ep_entradas"
    private const val SUFIJO_OCULTAS = "_ocultas"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun guardar(context: Context, fuenteId: String, referencia: String, texto: String) {
        val actuales = prefs(context).getStringSet(fuenteId, emptySet()) ?: emptySet()
        val claveNueva = referencia.trim().lowercase()
        val sinDuplicado = actuales.filterNot {
            it.split("|||", limit = 2).getOrNull(0)?.trim()?.lowercase() == claveNueva
        }
        val nuevaEntrada = "${referencia.trim()}|||${texto.trim()}"
        val actualizadas = (sinDuplicado + nuevaEntrada).toMutableSet()
        prefs(context).edit { putStringSet(fuenteId, actualizadas) }
        mostrar(context, fuenteId, referencia)
    }

    fun eliminar(context: Context, fuenteId: String, referencia: String) {
        val actuales = prefs(context).getStringSet(fuenteId, emptySet()) ?: emptySet()
        val clave = referencia.trim().lowercase()
        val filtradas = actuales.filterNot {
            it.split("|||", limit = 2).getOrNull(0)?.trim()?.lowercase() == clave
        }.toMutableSet()
        prefs(context).edit { putStringSet(fuenteId, filtradas) }
        ocultar(context, fuenteId, referencia)
    }

    fun ocultar(context: Context, fuenteId: String, referencia: String) {
        val clave = fuenteId + SUFIJO_OCULTAS
        val actuales = prefs(context).getStringSet(clave, emptySet()) ?: emptySet()
        val nuevas = (actuales + referencia.trim().lowercase()).toMutableSet()
        prefs(context).edit { putStringSet(clave, nuevas) }
    }

    fun mostrar(context: Context, fuenteId: String, referencia: String) {
        val clave = fuenteId + SUFIJO_OCULTAS
        val actuales = prefs(context).getStringSet(clave, emptySet()) ?: emptySet()
        val nuevas = actuales.filterNot { it == referencia.trim().lowercase() }.toMutableSet()
        prefs(context).edit { putStringSet(clave, nuevas) }
    }

    fun estaOculta(context: Context, fuenteId: String, referencia: String): Boolean {
        val clave = fuenteId + SUFIJO_OCULTAS
        val ocultas = prefs(context).getStringSet(clave, emptySet()) ?: emptySet()
        return ocultas.contains(referencia.trim().lowercase())
    }

    fun obtenerTexto(context: Context, fuenteId: String, referencia: String): String? {
        val entradas = prefs(context).getStringSet(fuenteId, emptySet()) ?: emptySet()
        val clave = referencia.trim().lowercase()
        for (entrada in entradas) {
            val partes = entrada.split("|||", limit = 2)
            if (partes.size == 2 && partes[0].trim().lowercase() == clave) {
                return partes[1]
            }
        }
        return null
    }

    fun obtenerTodas(context: Context, fuenteId: String): List<Pair<String, String>> {
        val entradas = prefs(context).getStringSet(fuenteId, emptySet()) ?: emptySet()
        return entradas.mapNotNull { entrada ->
            val partes = entrada.split("|||", limit = 2)
            if (partes.size == 2) partes[0] to partes[1] else null
        }.sortedBy { it.first.lowercase() }
    }
}
