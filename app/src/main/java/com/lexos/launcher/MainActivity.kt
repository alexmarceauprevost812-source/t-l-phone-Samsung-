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

        // Page d'accueil = seulement les apps choisies (ou les apps de base si rien de choisi)
        setupPager(homeApps(), favorites)
        setupDrawer(allApps)
        setupDock()
        setupSwipeDown()
        setupInsets()
    }

    override fun onResume() {
        super.onResume()
        applyBackground()
        applyFontAndColors()
        // Redessine les grilles pour appliquer police/couleur aux noms d'apps
        refreshFavorites()
        findViewById<RecyclerView>(R.id.appsGrid).adapter?.notifyDataSetChanged()
        handler.post(clockTick)
        updateHomeWeather()
        updateNotifRow()
        // Se rafraichir quand une notification arrive/part
        LexNotifService.listener = { handler.post { updateNotifRow() } }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockTick)
        LexNotifService.listener = null
    }

    private fun updateClock() {
        clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dateView.text = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())
    }

    // Rangee des notifications sur l'accueil : icones des apps qui en ont.
    // Tap = ouvre le panneau de notifications Android.
    private fun updateNotifRow() {
        val row = findViewById<LinearLayout>(R.id.notifRow)
        row.removeAllViews()

        val enabled = androidx.core.app.NotificationManagerCompat
            .getEnabledListenerPackages(this).contains(packageName)

        if (!enabled) {
            // Pas encore autorise : petit bouton pour activer
            val tv = TextView(this)
            tv.text = "🔔 activer"
            tv.textSize = 12f
            tv.setTextColor(LexTheme.accent(this))
            tv.typeface = LexTheme.typeface(this)
            row.addView(tv)
            row.setOnClickListener {
                try {
                    val i = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    startActivity(i)
                } catch (_: Exception) {}
            }
            return
        }

        val pkgs = LexNotifService.packages
        if (pkgs.isEmpty()) {
            val tv = TextView(this)
            tv.text = "🔔 0"
            tv.textSize = 12f
            tv.setTextColor(LexTheme.textColor(this))
            tv.typeface = LexTheme.typeface(this)
            row.addView(tv)
        } else {
            val byPkg = allApps.associateBy { it.packageName }
            for (pkg in pkgs.take(5)) {
                val iv = ImageView(this)
                val size = dp(26)
                val lp = LinearLayout.LayoutParams(size, size)
                lp.marginEnd = dp(6)
                iv.layoutParams = lp
                val icon = byPkg[pkg]?.icon ?: try { packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
                iv.setImageDrawable(icon)
                row.addView(iv)
            }
            if (LexNotifService.count > 0) {
                val tv = TextView(this)
                tv.text = "${LexNotifService.count}"
                tv.textSize = 12f
                tv.setTextColor(LexTheme.accent(this))
                tv.typeface = LexTheme.typeface(this)
                row.addView(tv)
            }
        }

        // Tap = ouvrir le panneau de notifications du telephone
        row.setOnClickListener { openNotificationPanel() }
    }

    // ----- Panneau de notifications LexOS (glisse depuis le haut) -----

    private var notifOpen = false

    private fun openNotificationPanel() {
        val panel = findViewById<View>(R.id.notifPanel)
        val sheet = findViewById<View>(R.id.notifSheet)
        fillNotifList()

        panel.alpha = 0f
        panel.visibility = View.VISIBLE
        panel.animate().alpha(1f).setDuration(180).start()

        sheet.translationY = -sheet.height.toFloat().coerceAtLeast(600f)
        sheet.animate()
            .translationY(0f)
            .setDuration(320)
            .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
            .start()
        notifOpen = true

        // Tap sur le fond sombre = fermer
        panel.setOnClickListener { closeNotificationPanel() }
        sheet.setOnClickListener { /* bloque le tap pour ne pas fermer */ }
        findViewById<TextView>(R.id.notifClearAll).setOnClickListener {
            for (n in LexNotifService.items) notifService?.dismiss(n.key)
            handler.postDelayed({ fillNotifList() }, 200)
        }
    }

    private fun closeNotificationPanel() {
        if (!notifOpen) return
        val panel = findViewById<View>(R.id.notifPanel)
        val sheet = findViewById<View>(R.id.notifSheet)
        sheet.animate()
            .translationY(-sheet.height.toFloat())
            .setDuration(260)
            .setInterpolator(android.view.animation.AccelerateInterpolator(1.5f))
            .start()
        panel.animate().alpha(0f).setDuration(260)
            .withEndAction { panel.visibility = View.GONE }
            .start()
        notifOpen = false
    }

    private val notifService: LexNotifService?
        get() = LexNotifService.instance

    private fun fillNotifList() {
        val list = findViewById<LinearLayout>(R.id.notifList)
        list.removeAllViews()
        findViewById<TextView>(R.id.notifPanelTitle).typeface = LexTheme.typeface(this)

        val items = LexNotifService.items
        if (items.isEmpty()) {
            val tv = TextView(this)
            tv.text = "Aucune notification"
            tv.setTextColor(Color.parseColor("#888888"))
            tv.textSize = 14f
            tv.typeface = LexTheme.typeface(this)
            tv.setPadding(dp(8), dp(20), dp(8), dp(20))
            list.addView(tv)
            return
        }

        val byPkg = allApps.associateBy { it.packageName }
        for (n in items) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_notif, list, false)
            val icon = row.findViewById<ImageView>(R.id.nIcon)
            val app = row.findViewById<TextView>(R.id.nApp)
            val title = row.findViewById<TextView>(R.id.nTitle)
            val text = row.findViewById<TextView>(R.id.nText)

            val info = byPkg[n.pkg]
            icon.setImageDrawable(info?.icon ?: try { packageManager.getApplicationIcon(n.pkg) } catch (_: Exception) { null })
            app.text = info?.label ?: n.pkg
            title.text = n.title
            text.text = n.text
            text.visibility = if (n.text.isBlank()) View.GONE else View.VISIBLE

            val tf = LexTheme.typeface(this)
            app.typeface = tf; title.typeface = tf; text.typeface = tf
            app.setTextColor(LexTheme.accent(this))

            // Tap = ouvrir l'app ; glisser = effacer
            row.setOnClickListener {
                closeNotificationPanel()
                AppRepository.launch(this, n.pkg)
            }
            row.setOnLongClickListener {
                row.animate().translationX(row.width.toFloat()).alpha(0f).setDuration(220)
                    .withEndAction {
                        notifService?.dismiss(n.key)
                        list.removeView(row)
                    }.start()
                true
            }
            list.addView(row)
        }
    }

    private var lastWeatherFetch = 0L

    // Meteo sur l'accueil, rafraichie au plus toutes les 15 min
    private fun updateHomeWeather() {
        val now = System.currentTimeMillis()
        if (now - lastWeatherFetch < 15 * 60 * 1000) return
        lastWeatherFetch = now
        val tv = findViewById<TextView>(R.id.homeWeather)
        WeatherFetcher.fetch(null, null) { txt ->
            tv.text = txt
            tv.typeface = LexTheme.typeface(this)
        }
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
        val current = pager.currentItem
        pager.adapter = HomePagerAdapter(
            homeApps, favorites,
            onOpen = { app, icon -> LaunchAnim.openWithAnimation(this, icon, app.packageName) },
            onLongClick = { app -> showFavoriteDialog(app) }
        )
        pager.setCurrentItem(current, false)

        // Onglets Accueil / Favoris : tap pour changer de page (en plus du glissement)
        val tabHome = findViewById<TextView>(R.id.tabHome)
        val tabFav = findViewById<TextView>(R.id.tabFav)
        tabHome.setOnClickListener { pager.setCurrentItem(0, true) }
        tabFav.setOnClickListener { pager.setCurrentItem(1, true) }

        fun paintTabs(pos: Int) {
            val on = LexTheme.accent(this)
            if (pos == 0) {
                tabHome.setBackgroundResource(R.drawable.logo_circle)
                tabHome.setTextColor(Color.parseColor("#0A0A0A"))
                tabFav.background = null
                tabFav.setTextColor(Color.WHITE)
            } else {
                tabFav.setBackgroundResource(R.drawable.logo_circle)
                tabFav.setTextColor(Color.parseColor("#0A0A0A"))
                tabHome.background = null
                tabHome.setTextColor(Color.WHITE)
            }
            tabHome.typeface = LexTheme.typeface(this)
            tabFav.typeface = LexTheme.typeface(this)
        }
        paintTabs(current)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = paintTabs(position)
        })
    }

    // Appui long sur une app = ajouter / retirer des favoris ou du dock
    private fun showFavoriteDialog(app: AppInfo) {
        val onHome = LexTheme.isOnHome(this, app.packageName)
        val isFav = LexTheme.isFavorite(this, app.packageName)
        val inDock = LexTheme.isInDock(this, app.packageName)
        val homeAction = if (onHome) "🏠 Retirer de l'accueil" else "🏠 Ajouter à l'accueil"
        val favAction = if (isFav) "⭐ Retirer des favoris" else "⭐ Ajouter aux favoris"
        val dockAction = if (inDock) "⬇ Retirer du dock" else "⬇ Ajouter au dock"
        val isLeft = LexTheme.leftApp(this) == app.packageName
        val leftAction = if (isLeft) "⚡ Retirer de l'app rapide (←)" else "⚡ App rapide (glisse ←)"
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.LexDialog)
            .setTitle(app.label)
            .setItems(arrayOf(homeAction, favAction, dockAction, leftAction, "Annuler")) { _, which ->
                when (which) {
                    0 -> {
                        if (onHome) LexTheme.removeHome(this, app.packageName)
                        else LexTheme.addHome(this, app.packageName)
                        refreshFavorites()
                    }
                    1 -> {
                        if (isFav) LexTheme.removeFavorite(this, app.packageName)
                        else LexTheme.addFavorite(this, app.packageName)
                        refreshFavorites()
                    }
                    2 -> {
                        if (inDock) LexTheme.removeDock(this, app.packageName)
                        else LexTheme.addDock(this, app.packageName)
                        setupDock()
                    }
                    3 -> {
                        LexTheme.setLeftApp(this, if (isLeft) null else app.packageName)
                        val msg = if (isLeft) "App rapide retirée"
                            else "${app.label} → glisse vers la gauche"
                        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    // Apps a montrer sur l'accueil : celles choisies par l'utilisateur,
    // sinon les apps de base (telephone, messages, photo...) pour ne pas etre vide.
    private fun homeApps(): List<AppInfo> {
        val byPkg = allApps.associateBy { it.packageName }
        val chosen = LexTheme.homePackages(this).mapNotNull { byPkg[it] }
        return chosen.ifEmpty { AppRepository.officialApps(this).ifEmpty { allApps.take(8) } }
    }

    private fun refreshFavorites() {
        val favorites = AppRepository.userFavorites(this, allApps).ifEmpty { allApps.take(3) }
        setupPager(homeApps(), favorites)
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
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                // Glisser vers le bas = toutes les apps
                if (dy > 60 && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                    sheet.state = BottomSheetBehavior.STATE_EXPANDED
                    return true
                }
                // Glisser vers la gauche = lancer l'app rapide choisie
                if (dx < -120 && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    launchLeftApp()
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
        val dockScroll = findViewById<View>(R.id.dockScroll)
        ViewCompat.setOnApplyWindowInsetsListener(dockScroll) { v, insets ->
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

    // Lance l'app rapide du geste gauche : celle choisie, sinon l'appareil photo.
    private fun launchLeftApp() {
        val chosen = LexTheme.leftApp(this)
        val pkg = chosen ?: AppRepository.resolveDock(this, "camera")?.packageName
        if (pkg != null) {
            AppRepository.launch(this, pkg)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

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
        if (notifOpen) { closeNotificationPanel(); return }
        if (::sheet.isInitialized && sheet.state != BottomSheetBehavior.STATE_HIDDEN) {
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}
