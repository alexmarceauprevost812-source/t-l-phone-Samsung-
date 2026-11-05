package com.lexos.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private var items: List<AppInfo>,
    private val onOpen: (AppInfo, View) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.favIcon)
        val name: TextView = view.findViewById(R.id.favName)
        val open: TextView = view.findViewById(R.id.favOpen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label
        holder.name.typeface = LexTheme.typeface(holder.itemView.context)
        holder.name.setTextColor(LexTheme.textColor(holder.itemView.context))
        holder.open.typeface = LexTheme.typeface(holder.itemView.context)
        holder.open.setOnClickListener { onOpen(app, holder.icon) }
        holder.icon.setOnClickListener { onOpen(app, holder.icon) }
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<AppInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
