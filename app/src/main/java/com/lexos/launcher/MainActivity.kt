package com.lexos.launcher

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private lateinit var sheet: BottomSheetBehavior<NestedScrollView>
    private lateinit var dots: LinearLayout
    private lateinit var brand: TextView
    private var favCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        brand = findViewById(R.id.brand)
        val all = AppRepository.loadAll(this)

        setupFavorites(all)
        setupDrawer(all)
        setupDock()
        setupSettingsGesture()
    }

    override fun onResume() {
        super.onResume()
        // Reapplique le fond et les couleurs au retour des reglages
        applyBackground()
        applyColors()
    }

    private fun applyBackground() {
        val rootView = findViewById<View>(android.R.id.content)
        val wall = LexTheme.wallpaper(this)
        val dim = LexTheme.dim(this)

        if (wall != null) {
            try {
                val input = contentResolver.openInputStream(Uri.parse(wall))
                val bmp = android.graphics.BitmapFactory.decodeStream(input)
                input?.close()
                val photo: Drawable = BitmapDrawable(resources, bmp)
                val veil = ColorDrawable(Color.argb((dim * 255 / 100), 0, 0, 0))
                rootView.background = LayerDrawable(arrayOf(photo, veil))
                return
            } catch (e: Exception) {
                // image introuvable : on retombe sur la couleur
            }
        }
        rootView.setBackgroundColor(LexTheme.bgColor(this))
    }

    private fun applyColors() {
        brand.setTextColor(LexTheme.textColor(this))
    }

    private fun setupFavorites(all: List<AppInfo>) {
        val pager = findViewById<ViewPager2>(R.id.favPager)
        dots = findViewById(R.id.dots)

        val favorites = AppRepository.favorites(all).ifEmpty { all.take(3) }
        favCount = favorites.size

        pager.adapter = FavoritesAdapter(favorites) { app, icon ->
            LaunchAnim.openWithAnimation(this, icon, app.packageName)
        }

        buildDots(favCount)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateDots(position)
        })
        updateDots(0)
    }

    private fun buildDots(count: Int) {
        dots.removeAllViews()
        repeat(count) {
            val dot = View(this)
            val lp = LinearLayout.LayoutParams(dp(6), dp(6))
            lp.marginStart = dp(4); lp.marginEnd = dp(4)
            dot.layoutParams = lp
            dot.setBackgroundResource(R.drawable.handle)
            dot.alpha = 0.3f
            dots.addView(dot)
        }
    }

    private fun updateDots(active: Int) {
        for (i in 0 until dots.childCount) {
            val dot = dots.getChildAt(i)
            val lp = dot.layoutParams as LinearLayout.LayoutParams
            if (i == active) { lp.width = dp(22); dot.alpha = 1f }
            else { lp.width = dp(6); dot.alpha = 0.3f }
            dot.layoutParams = lp
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
            iv.setOnClickListener {
                LaunchAnim.openWithAnimation(this, iv, app.packageName)
            }
            dock.addView(iv)
        }
    }

    // Appui long sur le nom LexOS = ouvrir les reglages
    private fun setupSettingsGesture() {
        brand.setOnLongClickListener {
            SettingsActivity.open(this)
            true
        }
        brand.setOnClickListener {
            Toast.makeText(this, "Maintiens LexOS pour les réglages", Toast.LENGTH_SHORT).show()
        }
    }

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
        // Sinon on reste sur l'accueil (comportement launcher)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
