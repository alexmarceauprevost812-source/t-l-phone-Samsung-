package com.lexos.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private var items: List<AppInfo>,
    private val onClick: (AppInfo, View) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null
) : RecyclerView.Adapter<AppsAdapter.VH>() {

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
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label
        holder.name.typeface = LexTheme.typeface(holder.itemView.context)
        holder.name.setTextColor(LexTheme.textColor(holder.itemView.context))
        holder.itemView.setOnClickListener { onClick(app, holder.icon) }
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(app)
            onLongClick != null
        }
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<AppInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
