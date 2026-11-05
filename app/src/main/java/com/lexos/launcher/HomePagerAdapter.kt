package com.lexos.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

// 2 pages horizontales : page 0 = accueil (apps officielles), page 1 = favoris gros plan.
class HomePagerAdapter(
    private val official: List<AppInfo>,
    private val favorites: List<AppInfo>,
    private val onOpen: (AppInfo, View) -> Unit
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
            grid.adapter = AppsAdapter(official) { app, icon -> onOpen(app, icon) }
        } else {
            val fav = holder.itemView.findViewById<ViewPager2>(R.id.favPager)
            fav.adapter = FavoritesAdapter(favorites) { app, icon -> onOpen(app, icon) }
        }
    }
}
