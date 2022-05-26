package com.example.runnerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runnerapp.R
import com.example.runnerapp.models.NavigationDrawerItem

class NavDrawerAdapter(

    private val itemsNavigation: ArrayList<NavigationDrawerItem>,
    private var clickNavigationDrawerMenu: OnClickNavigationDrawerMenu? = null,
) :
    RecyclerView.Adapter<NavDrawerAdapter.ItemViewHolder>() {

    private var selectedItem = 0

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemViewNavigation: LinearLayout = itemView.findViewById(R.id.item_view)
        private val textViewNavigation: TextView = itemView.findViewById(R.id.text_view_navigation)
        private val imageViewNavigation = itemView.findViewById<ImageView>(R.id.image_view_navigation)

        fun bind (holder: ItemViewHolder, selectedItem: Int) {
            holder.textViewNavigation.text = itemsNavigation[adapterPosition].name
            holder.imageViewNavigation.setImageResource(itemsNavigation[adapterPosition].image)

        }
    }

    interface OnClickNavigationDrawerMenu {
        fun sendClickPosition(selectedNavItem: NavigationDrawerItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.navigation_drawer_item, parent, false)

        clickNavigationDrawerMenu = try {
            parent.context as OnClickNavigationDrawerMenu
        } catch (e: ClassCastException) {
            throw ClassCastException("${parent.context} must implement interface OnClickNavigationDrawerMenu")
        }
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        selectedItem = holder.adapterPosition

        holder.bind(holder, holder.adapterPosition)
        setItemViewNavigationOnClickListener(holder)
    }


    private fun setItemViewNavigationOnClickListener(holder: ItemViewHolder) {
        holder.itemViewNavigation.setOnClickListener {
            val clickNavigationDrawerMenu = clickNavigationDrawerMenu ?: return@setOnClickListener

            val previousItem = selectedItem
            selectedItem = holder.adapterPosition
            val itemColor = holder.itemViewNavigation.context.resources.getColor(
                if (selectedItem == holder.adapterPosition)
                    R.color.color_checked_item
                else
                    R.color.color_unchecked_item
            )

            holder.itemView.setBackgroundColor(itemColor)

            notifyItemChanged(previousItem)
            notifyItemChanged(selectedItem)
            clickNavigationDrawerMenu.sendClickPosition(itemsNavigation[selectedItem])
        }
    }

    override fun getItemCount(): Int {
        return itemsNavigation.size
    }
}