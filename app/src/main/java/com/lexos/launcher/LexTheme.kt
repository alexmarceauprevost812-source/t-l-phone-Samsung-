package com.lexos.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface

// Gere tous les reglages visuels de LexOS, sauvegardes sur le telephone.
object LexTheme {

    private const val PREFS = "lexos_prefs"

    // Cles
    private const val K_MODE = "mode"           // "soir" ou "jour"
    private const val K_ACCENT = "accent"       // couleur des boutons / accent
    private const val K_TEXT = "text"           // couleur du texte
    private const val K_BG = "bg"               // couleur de fond principale
    private const val K_WALL = "wallpaper"      // URI de la photo de fond (ou null)
    private const val K_DIM = "dim"             // voile sombre sur la photo (0-80)
    private const val K_FONT = "font"           // police : normal, mono, rond, techno
    private const val K_MATRIX = "matrix"       // effet matrix active (true/false)

    // Valeurs par defaut (theme soir LexOS)
    const val DEFAULT_ACCENT = "#FF6A00"
    private const val DEFAULT_TEXT_SOIR = "#FFFFFF"
    private const val DEFAULT_BG_SOIR = "#0A0A0A"
    private const val DEFAULT_TEXT_JOUR = "#121212"
    private const val DEFAULT_BG_JOUR = "#F2F2F2"

    // Palettes proposees dans les reglages
    val ACCENT_CHOICES = listOf(
        "#FF6A00", // orange LexOS
        "#FF3B30", // rouge
        "#FFD60A", // jaune
        "#34C759", // vert
        "#0A84FF", // bleu
        "#BF5AF2", // violet
        "#FF2D92", // rose
        "#FFFFFF"  // blanc
    )

    fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isSoir(ctx: Context): Boolean = prefs(ctx).getString(K_MODE, "soir") == "soir"

    fun setMode(ctx: Context, soir: Boolean) {
        prefs(ctx).edit().putString(K_MODE, if (soir) "soir" else "jour").apply()
    }

    fun accent(ctx: Context): Int =
        parse(prefs(ctx).getString(K_ACCENT, DEFAULT_ACCENT), DEFAULT_ACCENT)

    fun setAccent(ctx: Context, hex: String) {
        prefs(ctx).edit().putString(K_ACCENT, hex).apply()
    }

    fun textColor(ctx: Context): Int {
        val saved = prefs(ctx).getString(K_TEXT, null)
        if (saved != null) return parse(saved, DEFAULT_TEXT_SOIR)
        return parse(if (isSoir(ctx)) DEFAULT_TEXT_SOIR else DEFAULT_TEXT_JOUR, DEFAULT_TEXT_SOIR)
    }

    fun setTextColor(ctx: Context, hex: String) {
        prefs(ctx).edit().putString(K_TEXT, hex).apply()
    }

    fun bgColor(ctx: Context): Int {
        val saved = prefs(ctx).getString(K_BG, null)
        if (saved != null) return parse(saved, DEFAULT_BG_SOIR)
        return parse(if (isSoir(ctx)) DEFAULT_BG_SOIR else DEFAULT_BG_JOUR, DEFAULT_BG_SOIR)
    }

    fun setBgColor(ctx: Context, hex: String) {
        prefs(ctx).edit().putString(K_BG, hex).apply()
    }

    // Photo de fond choisie par l'utilisateur (null = pas de photo, on utilise la couleur)
    fun wallpaper(ctx: Context): String? = prefs(ctx).getString(K_WALL, null)

    fun setWallpaper(ctx: Context, uri: String?) {
        val e = prefs(ctx).edit()
        if (uri == null) e.remove(K_WALL) else e.putString(K_WALL, uri)
        e.apply()
    }

    // Voile sombre sur la photo, de 0 (rien) a 80 (tres sombre)
    fun dim(ctx: Context): Int = prefs(ctx).getInt(K_DIM, 35)

    fun setDim(ctx: Context, value: Int) {
        prefs(ctx).edit().putInt(K_DIM, value.coerceIn(0, 80)).apply()
    }

    // Police d'ecriture
    val FONT_CHOICES = listOf("normal", "mono", "rond", "techno")
    val FONT_LABELS = listOf("Normale", "Monospace", "Arrondie", "Techno")

    fun fontKey(ctx: Context): String = prefs(ctx).getString(K_FONT, "normal") ?: "normal"

    fun setFont(ctx: Context, key: String) {
        prefs(ctx).edit().putString(K_FONT, key).apply()
    }

    fun typeface(ctx: Context): Typeface = when (fontKey(ctx)) {
        "mono" -> Typeface.MONOSPACE
        "rond" -> Typeface.create("sans-serif-rounded", Typeface.NORMAL)
        "techno" -> Typeface.create("sans-serif-condensed", Typeface.BOLD)
        else -> Typeface.DEFAULT
    }

    // Effet Matrix en fond
    fun matrixOn(ctx: Context): Boolean = prefs(ctx).getBoolean(K_MATRIX, true)

    fun setMatrix(ctx: Context, on: Boolean) {
        prefs(ctx).edit().putBoolean(K_MATRIX, on).apply()
    }

    // Favoris choisis par l'utilisateur (noms de paquets, dans l'ordre d'ajout)
    private const val K_FAVS = "favs"

    fun favoritePackages(ctx: Context): List<String> {
        val raw = prefs(ctx).getString(K_FAVS, "") ?: ""
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun isFavorite(ctx: Context, pkg: String): Boolean =
        favoritePackages(ctx).contains(pkg)

    fun addFavorite(ctx: Context, pkg: String) {
        val list = favoritePackages(ctx).toMutableList()
        if (!list.contains(pkg)) list.add(pkg)
        prefs(ctx).edit().putString(K_FAVS, list.joinToString(",")).apply()
    }

    fun removeFavorite(ctx: Context, pkg: String) {
        val list = favoritePackages(ctx).toMutableList()
        list.remove(pkg)
        prefs(ctx).edit().putString(K_FAVS, list.joinToString(",")).apply()
    }

    // Remet le mode jour ou soir avec ses couleurs par defaut (efface les choix manuels)
    fun applyModeDefaults(ctx: Context, soir: Boolean) {
        setMode(ctx, soir)
        prefs(ctx).edit()
            .remove(K_TEXT)
            .remove(K_BG)
            .apply()
    }

    private fun parse(hex: String?, fallback: String): Int =
        try { Color.parseColor(hex ?: fallback) } catch (e: Exception) { Color.parseColor(fallback) }
}
