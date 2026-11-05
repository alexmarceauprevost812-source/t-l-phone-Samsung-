package com.lexos.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var root: View
    private lateinit var previewText: TextView
    private lateinit var previewBtn: TextView
    private lateinit var previewCard: View

    // Ouvre la galerie et garde l'acces durable a l'image choisie
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
                LexTheme.setWallpaper(this, uri.toString())
                refreshPreview()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_settings)

        root = findViewById(R.id.settingsRoot)
        previewText = findViewById(R.id.previewText)
        previewBtn = findViewById(R.id.previewBtn)
        previewCard = findViewById(R.id.previewCard)

        setupModeButtons()
        setupColorRow(R.id.accentRow) { hex ->
            LexTheme.setAccent(this, hex); refreshPreview()
        }
        setupColorRow(R.id.textRow) { hex ->
            LexTheme.setTextColor(this, hex); refreshPreview()
        }
        setupColorRow(R.id.bgRow) { hex ->
            LexTheme.setBgColor(this, hex); refreshPreview()
        }
        setupWallpaper()
        setupFontRow()
        setupClockStyle()
        setupMatrix()
        setupReset()

        refreshPreview()
    }

    private fun setupModeButtons() {
        findViewById<TextView>(R.id.btnSoir).setOnClickListener {
            LexTheme.applyModeDefaults(this, true); refreshPreview()
        }
        findViewById<TextView>(R.id.btnJour).setOnClickListener {
            LexTheme.applyModeDefaults(this, false); refreshPreview()
        }
    }

    private fun setupColorRow(rowId: Int, onPick: (String) -> Unit) {
        val row = findViewById<LinearLayout>(rowId)
        row.removeAllViews()
        for (hex in LexTheme.ACCENT_CHOICES) {
            val swatch = ImageView(this)
            val size = dp(40)
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = dp(10)
            swatch.layoutParams = lp
            val circle = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(hex))
                setStroke(dp(1), Color.parseColor("#44FFFFFF"))
            }
            swatch.background = circle
            swatch.setOnClickListener { onPick(hex) }
            row.addView(swatch)
        }
    }

    private fun setupWallpaper() {
        findViewById<TextView>(R.id.btnPickImage).setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }
        findViewById<TextView>(R.id.btnClearImage).setOnClickListener {
            LexTheme.setWallpaper(this, null); refreshPreview()
        }
        val bar = findViewById<SeekBar>(R.id.dimBar)
        bar.progress = LexTheme.dim(this)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                LexTheme.setDim(this@SettingsActivity, p); refreshPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun setupFontRow() {
        val row = findViewById<LinearLayout>(R.id.fontRow)
        row.removeAllViews()
        for (i in LexTheme.FONT_CHOICES.indices) {
            val key = LexTheme.FONT_CHOICES[i]
            val tv = TextView(this)
            tv.text = LexTheme.FONT_LABELS[i]
            tv.textSize = 16f
            tv.setPadding(dp(14), dp(12), dp(14), dp(12))
            tv.typeface = when (key) {
                "mono" -> android.graphics.Typeface.MONOSPACE
                "rond" -> android.graphics.Typeface.create("sans-serif-rounded", android.graphics.Typeface.NORMAL)
                "techno" -> android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                else -> android.graphics.Typeface.DEFAULT
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = dp(8)
            tv.layoutParams = lp
            tv.setBackgroundResource(R.drawable.tile_app)
            tv.setOnClickListener { LexTheme.setFont(this, key); refreshPreview() }
            row.addView(tv)
        }
    }

    private fun setupClockStyle() {
        findViewById<TextView>(R.id.btnClockNum).setOnClickListener {
            LexTheme.setClockStyle(this, "num")
        }
        findViewById<TextView>(R.id.btnClockAnalog).setOnClickListener {
            LexTheme.setClockStyle(this, "aiguille")
        }
    }

    private fun setupMatrix() {
        findViewById<TextView>(R.id.btnMatrixOn).setOnClickListener {
            LexTheme.setMatrix(this, true); refreshPreview()
        }
        findViewById<TextView>(R.id.btnMatrixOff).setOnClickListener {
            LexTheme.setMatrix(this, false); refreshPreview()
        }
    }

    private fun setupReset() {
        // Raccourci vers tous les parametres Samsung / Android
        findViewById<TextView>(R.id.btnSamsungSettings).setOnClickListener {
            try {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            } catch (_: Exception) {}
        }
        findViewById<TextView>(R.id.btnReset).setOnClickListener {
            LexTheme.applyModeDefaults(this, true)
            LexTheme.setAccent(this, LexTheme.DEFAULT_ACCENT)
            LexTheme.setWallpaper(this, null)
            LexTheme.setDim(this, 35)
            refreshPreview()
        }
    }

    // Applique en direct tous les choix a l'ecran de reglages et a l'apercu
    private fun refreshPreview() {
        val accent = LexTheme.accent(this)
        val text = LexTheme.textColor(this)
        val bg = LexTheme.bgColor(this)

        root.setBackgroundColor(bg)

        // Titre et libelles suivent la couleur de texte
        colorAllText(root as ViewGroup, text)

        // Apercu
        previewCard.setBackgroundColor(bg)
        previewText.setTextColor(text)
        val btnBg = GradientDrawable().apply {
            cornerRadius = dp(30).toFloat()
            setColor(accent)
        }
        previewBtn.background = btnBg
        previewBtn.setTextColor(contrastOn(accent))

        // Applique la police choisie a l'apercu
        val tf = LexTheme.typeface(this)
        previewText.typeface = tf
        previewBtn.typeface = tf

        // Marque les boutons Matrix actif/inactif
        val on = LexTheme.matrixOn(this)
        findViewById<TextView>(R.id.btnMatrixOn)?.alpha = if (on) 1f else 0.4f
        findViewById<TextView>(R.id.btnMatrixOff)?.alpha = if (on) 0.4f else 1f
    }

    // Met la couleur de texte sur les TextView "normaux" (pas les pastilles ni boutons colores)
    private fun colorAllText(group: ViewGroup, color: Int) {
        for (i in 0 until group.childCount) {
            val v = group.getChildAt(i)
            when (v) {
                is TextView -> {
                    if (v.id != R.id.previewBtn) v.setTextColor(color)
                }
                is ViewGroup -> colorAllText(v, color)
            }
        }
    }

    // Noir ou blanc selon la luminosite de la couleur de bouton, pour rester lisible
    private fun contrastOn(color: Int): Int {
        val lum = (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255
        return if (lum > 0.6) Color.parseColor("#0A0A0A") else Color.WHITE
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        fun open(activity: Activity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}
