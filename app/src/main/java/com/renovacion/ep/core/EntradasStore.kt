package com.renovacion.ep.core

import android.content.Context
import androidx.core.content.edit

object EntradasStore {
    private const val PREFS_NAME = "ep_entradas"

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
}
