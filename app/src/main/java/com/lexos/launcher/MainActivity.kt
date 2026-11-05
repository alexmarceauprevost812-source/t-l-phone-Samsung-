package com.lexos.launcher

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sheet: BottomSheetBehavior<NestedScrollView>
    private lateinit var clock: TextView
    private lateinit var dateView: TextView
    private lateinit var matrix: MatrixView
    private var allApps: List<AppInfo> = emptyList()
    private val handler = Handler(Looper.getMainLooper())

    private val clockTick = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 10_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        clock = findViewById(R.id.clock)
        dateView = findViewById(R.id.date)
        matrix = findViewById(R.id.matrix)

        allApps = AppRepository.loadAll(this)
        val favorites = AppRepository.userFavorites(this, allApps).ifEmpty { allApps.take(3) }

        // Page d'accueil = toutes les apps
        setupPager(allApps, favorites)
        setupDrawer(allApps)
        setupDock()
        setupSwipeDown()
        setupInsets()
    }

    override fun onResume() {
        super.onResume()
        applyBackground()
        applyFontAndColors()
        handler.post(clockTick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockTick)
    }

    private fun updateClock() {
        clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dateView.text = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())
    }

    private fun applyBackground() {
        val root = findViewById<View>(android.R.id.content)
        val wall = LexTheme.wallpaper(this)
        val dim = LexTheme.dim(this)

        matrix.visibility = if (LexTheme.matrixOn(this)) View.VISIBLE else View.GONE
        matrix.setColor(colorToHex(LexTheme.accent(this)))

        if (wall != null) {
            try {
                val input = contentResolver.openInputStream(Uri.parse(wall))
                val bmp = android.graphics.BitmapFactory.decodeStream(input)
                input?.close()
                val photo: Drawable = BitmapDrawable(resources, bmp)
                val veil = ColorDrawable(Color.argb(dim * 255 / 100, 0, 0, 0))
                root.background = LayerDrawable(arrayOf(photo, veil))
                return
            } catch (_: Exception) {}
        }
        root.setBackgroundColor(LexTheme.bgColor(this))
    }

    private fun applyFontAndColors() {
        val tf = LexTheme.typeface(this)
        clock.typeface = tf
        dateView.typeface = tf
        clock.setTextColor(LexTheme.textColor(this))
    }

    private fun setupPager(homeApps: List<AppInfo>, favorites: List<AppInfo>) {
        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = HomePagerAdapter(
            homeApps, favorites,
            onOpen = { app, icon -> LaunchAnim.openWithAnimation(this, icon, app.packageName) },
            onLongClick = { app -> showFavoriteDialog(app) }
        )
    }

    // Appui long sur une app = ajouter / retirer des favoris ou du dock
    private fun showFavoriteDialog(app: AppInfo) {
        val isFav = LexTheme.isFavorite(this, app.packageName)
        val inDock = LexTheme.isInDock(this, app.packageName)
        val favAction = if (isFav) "Retirer des favoris" else "Ajouter aux favoris"
        val dockAction = if (inDock) "Retirer du dock" else "Ajouter au dock"
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.LexDialog)
            .setTitle(app.label)
            .setItems(arrayOf(favAction, dockAction, "Annuler")) { _, which ->
                when (which) {
                    0 -> {
                        if (isFav) LexTheme.removeFavorite(this, app.packageName)
                        else LexTheme.addFavorite(this, app.packageName)
                        refreshFavorites()
                    }
                    1 -> {
                        if (inDock) LexTheme.removeDock(this, app.packageName)
                        else LexTheme.addDock(this, app.packageName)
                        setupDock()
                    }
                }
            }
            .show()
    }

    private fun refreshFavorites() {
        val favorites = AppRepository.userFavorites(this, allApps).ifEmpty { allApps.take(3) }
        setupPager(allApps, favorites)
    }

    private fun setupDrawer(all: List<AppInfo>) {
        val drawer = findViewById<NestedScrollView>(R.id.drawer)
        val grid = findViewById<RecyclerView>(R.id.appsGrid)
        grid.layoutManager = GridLayoutManager(this, 4)
        grid.adapter = AppsAdapter(all, { app, icon ->
            LaunchAnim.openWithAnimation(this, icon, app.packageName)
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }, { app -> showFavoriteDialog(app) })
        sheet = BottomSheetBehavior.from(drawer)
        sheet.isHideable = true
        sheet.peekHeight = 0
        sheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupDock() {
        val dock = findViewById<LinearLayout>(R.id.dock)
        dock.removeAllViews()

        // 1. Bouton Reglages LexOS
        val settingsBtn = LayoutInflater.from(this)
            .inflate(R.layout.item_dock, dock, false) as ImageView
        settingsBtn.setImageResource(R.drawable.ic_lex_settings)
        settingsBtn.setOnClickListener { SettingsActivity.open(this) }
        dock.addView(settingsBtn)

        // 2. Telephone + Contacts
        for (role in listOf("dialer", "contacts")) {
            val app = AppRepository.resolveDock(this, role) ?: continue
            addDockIcon(dock, app)
        }

        // 3. Apps choisies par l'utilisateur pour le dock
        val byPkg = allApps.associateBy { it.packageName }
        for (pkg in LexTheme.dockPackages(this)) {
            val app = byPkg[pkg] ?: continue
            addDockIcon(dock, app)
        }
    }

    private fun addDockIcon(dock: LinearLayout, app: AppInfo) {
        val iv = LayoutInflater.from(this)
            .inflate(R.layout.item_dock, dock, false) as ImageView
        iv.setImageDrawable(app.icon)
        iv.setOnClickListener { LaunchAnim.openWithAnimation(this, iv, app.packageName) }
        dock.addView(iv)
    }

    // Glisser vers le bas sur l'horloge/haut d'ecran = ouvrir toutes les apps.
    // Appui long sur l'horloge = reglages.
    private fun setupSwipeDown() {
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 != null && e2.y - e1.y > 60) {
                    sheet.state = BottomSheetBehavior.STATE_EXPANDED
                    return true
                }
                return false
            }
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Tap sur l'en-tete = ouvrir toutes les apps (secours si le glissement rate)
                sheet.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
            override fun onLongPress(e: MotionEvent) {
                SettingsActivity.open(this@MainActivity)
            }
        })
        val header = findViewById<View>(R.id.header)
        header.setOnTouchListener { _, ev -> detector.onTouchEvent(ev); true }
    }

    // Remonte le dock au-dessus de la barre de navigation, et descend l'en-tete
    // sous la barre d'etat, pour que rien ne soit coupe.
    private fun setupInsets() {
        val dock = findViewById<LinearLayout>(R.id.dock)
        ViewCompat.setOnApplyWindowInsetsListener(dock) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, dp(14), v.paddingRight, dp(18) + bars.bottom)
            insets
        }
        val header = findViewById<View>(R.id.header)
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, dp(16) + bars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun colorToHex(c: Int): String =
        String.format("#%06X", 0xFFFFFF and c)

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        if (::sheet.isInitialized && sheet.state != BottomSheetBehavior.STATE_HIDDEN) {
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::sheet.isInitialized && sheet.state != BottomSheetBehavior.STATE_HIDDEN) {
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}
