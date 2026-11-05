package com.lexos.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

// 2 pages : 0 = Accueil (apps, deplacables), 1 = Favoris (app en gros, plein ecran)
class HomePagerAdapter(
    private val home: List<AppInfo>,
    private val favorites: List<AppInfo>,
    private val onOpen: (AppInfo, View) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null,
    private val onHomeReordered: ((List<AppInfo>) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PageVH(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int) = position
    override fun getItemCount() = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = if (viewType == 0) R.layout.page_home else R.layout.page_favorites
        return PageVH(inflater.inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) bindHome(holder.itemView) else bindFavorites(holder.itemView)
    }

    private fun bindHome(view: View) {
        val grid = view.findViewById<RecyclerView>(R.id.homeGrid)
        grid.layoutManager = GridLayoutManager(view.context, 4)
        val adapter = AppsAdapter(home, { app, icon -> onOpen(app, icon) }, onLongClick)
        grid.adapter = adapter

        // Glisser-deposer : appui long sur une app pour la deplacer
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.move(vh.bindingAdapterPosition, target.bindingAdapterPosition)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
            override fun isLongPressDragEnabled() = false
            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                onHomeReordered?.invoke(adapter.data.toList())
            }
        }
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(grid)
        adapter.dragStarter = { vh -> helper.startDrag(vh) }
    }

    private fun bindFavorites(view: View) {
        val fav = view.findViewById<ViewPager2>(R.id.favPager)
        fav.orientation = ViewPager2.ORIENTATION_VERTICAL
        fav.adapter = FavoritesAdapter(favorites) { app, icon -> onOpen(app, icon) }

        fav.setPageTransformer { page, pos ->
            page.alpha = 1f - kotlin.math.abs(pos).coerceAtMost(1f)
            val scale = 0.85f + (1f - kotlin.math.abs(pos).coerceAtMost(1f)) * 0.15f
            page.scaleX = scale
            page.scaleY = scale
            page.translationY = -pos * page.height * 0.12f
        }

        val prev = view.findViewById<TextView>(R.id.favPrev)
        val next = view.findViewById<TextView>(R.id.favNext)
        val counter = view.findViewById<TextView>(R.id.favCounter)
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
    }
}
