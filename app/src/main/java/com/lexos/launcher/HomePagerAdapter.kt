package com.lexos.launcher

import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

// 2 pages horizontales : page 0 = accueil (apps officielles), page 1 = favoris gros plan.
class HomePagerAdapter(
    private val official: List<AppInfo>,
    private val favorites: List<AppInfo>,
    private val onOpen: (AppInfo, View) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PageVH(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            PageVH(inflater.inflate(R.layout.page_home, parent, false))
        } else {
            PageVH(inflater.inflate(R.layout.page_favorites, parent, false))
        }
    }

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            val grid = holder.itemView.findViewById<RecyclerView>(R.id.homeGrid)
            grid.layoutManager = GridLayoutManager(holder.itemView.context, 4)
            grid.adapter = AppsAdapter(official, { app, icon -> onOpen(app, icon) }, onLongClick)
        } else {
            val ctx = holder.itemView.context
            val fav = holder.itemView.findViewById<ViewPager2>(R.id.favPager)
            fav.orientation = ViewPager2.ORIENTATION_VERTICAL
            fav.adapter = FavoritesAdapter(favorites) { app, icon -> onOpen(app, icon) }

            // Fleches + compteur pour passer d'un favori a l'autre (fiable a 100 %)
            val prev = holder.itemView.findViewById<TextView>(R.id.favPrev)
            val next = holder.itemView.findViewById<TextView>(R.id.favNext)
            val counter = holder.itemView.findViewById<TextView>(R.id.favCounter)
            val total = favorites.size
            fun updateCounter() { counter.text = "${fav.currentItem + 1} / $total" }
            updateCounter()
            prev.setOnClickListener {
                if (total > 0) fav.setCurrentItem((fav.currentItem - 1 + total) % total, true)
            }
            next.setOnClickListener {
                if (total > 0) fav.setCurrentItem((fav.currentItem + 1) % total, true)
            }
            fav.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = updateCounter()
            })
            val hide = total <= 1
            prev.visibility = if (hide) View.GONE else View.VISIBLE
            next.visibility = if (hide) View.GONE else View.VISIBLE

            // Meteo (coordonnees par defaut, pas besoin de permission)
            val weather = holder.itemView.findViewById<TextView>(R.id.weatherText)
            WeatherFetcher.fetch(null, null) { txt ->
                weather.text = if (txt.isBlank()) "indisponible" else txt
            }

            // Notifications
            val notif = holder.itemView.findViewById<TextView>(R.id.notifText)
            val enabled = NotificationManagerCompat.getEnabledListenerPackages(ctx)
                .contains(ctx.packageName)
            if (!enabled) {
                notif.text = "Activer ▸"
                notif.setOnClickListener {
                    try {
                        val i = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(i)
                    } catch (_: Exception) {}
                }
            } else {
                val r = LexNotifService.recent
                notif.setOnClickListener(null)
                notif.text = if (r.isEmpty()) "Aucune"
                    else "${LexNotifService.count} • " + r.takeLast(2).joinToString(" · ")
            }
        }
    }
}
