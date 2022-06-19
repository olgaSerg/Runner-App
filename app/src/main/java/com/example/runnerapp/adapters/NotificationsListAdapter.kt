package com.example.runnerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runnerapp.R
import com.example.runnerapp.fragments.notifications.NotificationsListFragment
import com.example.runnerapp.models.NotificationModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsListAdapter(
    private val notifications: List<NotificationModel>,
    private val recyclerViewItemClickListener: NotificationsListFragment.OnNotificationItemClickListener
) : RecyclerView.Adapter<NotificationsListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.time_notification)
        val date: TextView = itemView.findViewById(R.id.date_notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterPosition = holder.adapterPosition

        bind(holder, adapterPosition)
    }

    private fun bind(holder: ViewHolder, adapterPosition: Int) {
        val position = notifications[adapterPosition]
        if (position.notifyAt != null) {
            val time = formatTime(position.notifyAt!!)
            val date = formatDate(position.notifyAt!!)
            holder.time.text = time
            holder.date.text = date
        }

        holder.itemView.setOnClickListener {
            val selectedNotification = notifications[adapterPosition]
            recyclerViewItemClickListener.onNotificationItemClick(selectedNotification)
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    private fun formatTime(time: Long): String {
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(time)
    }

    private fun formatDate(date: Long): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}