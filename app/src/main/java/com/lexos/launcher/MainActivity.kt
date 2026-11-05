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

        val all = AppRepository.loadAll(this)
        val official = AppRepository.officialApps(this).ifEmpty { all.take(8) }
        val favorites = AppRepository.favorites(all).ifEmpty { all.take(3) }

        setupPager(official, favorites)
        setupDrawer(all)
        setupDock()
        setupSwipeDown()
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

    private fun setupPager(official: List<AppInfo>, favorites: List<AppInfo>) {
        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = HomePagerAdapter(official, favorites) { app, icon ->
            LaunchAnim.openWithAnimation(this, icon, app.packageName)
        }
    }

    private fun setupDrawer(all: List<AppInfo>) {
        val drawer = findViewById<NestedScrollView>(R.id.drawer)
        val grid = findViewById<RecyclerView>(R.id.appsGrid)
        grid.layoutManager = GridLayoutManager(this, 4)
        grid.adapter = AppsAdapter(all) { app, icon ->
            LaunchAnim.openWithAnimation(this, icon, app.packageName)
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }
        sheet = BottomSheetBehavior.from(drawer)
        sheet.isHideable = true
        sheet.peekHeight = 0
        sheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupDock() {
        val dock = findViewById<LinearLayout>(R.id.dock)
        dock.removeAllViews()
        for (role in AppRepository.DOCK_PACKAGES) {
            val app = AppRepository.resolveDock(this, role) ?: continue
            val iv = LayoutInflater.from(this)
                .inflate(R.layout.item_dock, dock, false) as ImageView
            iv.setImageDrawable(app.icon)
            iv.setOnClickListener { LaunchAnim.openWithAnimation(this, iv, app.packageName) }
            dock.addView(iv)
        }
    }

    // Glisser vers le bas sur l'horloge/haut d'ecran = ouvrir toutes les apps.
    // Appui long sur l'horloge = reglages.
    private fun setupSwipeDown() {
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 != null && e2.y - e1.y > 120 && vy > 0) {
                    sheet.state = BottomSheetBehavior.STATE_EXPANDED
                    return true
                }
                return false
            }
            override fun onLongPress(e: MotionEvent) {
                SettingsActivity.open(this@MainActivity)
            }
        })
        val top = findViewById<View>(R.id.clock)
        top.setOnTouchListener { _, ev -> detector.onTouchEvent(ev); true }
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
        if (::sheet.isInitialized && sheet.state != BottomSheetBehavior.STATE_HIDDEN) {
            sheet.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}
