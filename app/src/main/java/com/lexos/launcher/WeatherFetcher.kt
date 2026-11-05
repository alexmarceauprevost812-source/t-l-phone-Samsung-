package com.lexos.launcher

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

// Recupere la meteo actuelle via Open-Meteo (gratuit, sans cle API).
object WeatherFetcher {

    // Coordonnees par defaut : Normandin, QC (utilisees si pas de localisation)
    private const val DEFAULT_LAT = 48.83
    private const val DEFAULT_LON = -72.53

    fun fetch(lat: Double?, lon: Double?, onResult: (String) -> Unit) {
        val la = lat ?: DEFAULT_LAT
        val lo = lon ?: DEFAULT_LON
        thread {
            val text = try {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$la&longitude=$lo&current=temperature_2m,weather_code"
                val json = URL(url).readText()
                val cur = JSONObject(json).getJSONObject("current")
                val temp = cur.getDouble("temperature_2m").toInt()
                val code = cur.getInt("weather_code")
                "${temp}°  ${label(code)}"
            } catch (e: Exception) {
                ""
            }
            Handler(Looper.getMainLooper()).post { onResult(text) }
        }
    }

    // Code meteo WMO -> petit texte
    private fun label(code: Int): String = when (code) {
        0 -> "Clair"
        1, 2, 3 -> "Nuageux"
        45, 48 -> "Brouillard"
        in 51..57 -> "Bruine"
        in 61..67 -> "Pluie"
        in 71..77 -> "Neige"
        in 80..82 -> "Averses"
        in 85..86 -> "Neige"
        in 95..99 -> "Orage"
        else -> ""
    }
}
