package com.lexos.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    items: List<AppInfo>,
    private val onClick: (AppInfo, View) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null,
    private val onMoved: ((List<AppInfo>) -> Unit)? = null
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    val data = items.toMutableList()
    // Si defini, l'appui long demarre le deplacement (drag) au lieu du menu
    var dragStarter: ((RecyclerView.ViewHolder) -> Unit)? = null

    // Deplace un element (pour le glisser-deposer) et sauvegarde le nouvel ordre
    fun move(from: Int, to: Int) {
        if (from < 0 || to < 0 || from >= data.size || to >= data.size) return
        val it = data.removeAt(from)
        data.add(to, it)
        notifyItemMoved(from, to)
    }
    fun commitMove() { onMoved?.invoke(data.toList()) }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = data[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label
        holder.name.typeface = LexTheme.typeface(holder.itemView.context)
        holder.name.setTextColor(LexTheme.textColor(holder.itemView.context))
        holder.itemView.setOnClickListener { onClick(app, holder.icon) }
        holder.itemView.setOnLongClickListener {
            val starter = dragStarter
            if (starter != null) {
                // Page d'accueil : l'appui long demarre le deplacement
                starter(holder)
                true
            } else {
                onLongClick?.invoke(app)
                onLongClick != null
            }
        }
    }

    override fun getItemCount() = data.size

    fun update(newItems: List<AppInfo>) {
        data.clear(); data.addAll(newItems)
        notifyDataSetChanged()
    }
}
