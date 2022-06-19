package com.example.runnerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runnerapp.R
import com.example.runnerapp.fragments.running.TracksListFragment
import com.example.runnerapp.models.TrackModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TracksListAdapter(
    private val tracks: List<TrackModel>,
    private val recyclerViewItemClickListener: TracksListFragment.OnTracksRecyclerViewItemClickListener
) : RecyclerView.Adapter<TracksListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterPosition = holder.adapterPosition

        bind(holder, adapterPosition)
    }

    private fun bind(holder: ViewHolder, adapterPosition: Int) {
        val date = tracks[adapterPosition].startAt?.let { formatDate(it) }
        holder.dataStart.text = date
        holder.distance.text = tracks[adapterPosition].distance?.let { formatDistance(it) }
        holder.duration.text = tracks[adapterPosition].duration?.let { timeToString(it) }

        holder.itemView.setOnClickListener {
            val selectedTrack = tracks[adapterPosition]
            recyclerViewItemClickListener.onTrackClick(selectedTrack)
        }
    }

    private fun formatDate(date: Date): String {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataStart: TextView = itemView.findViewById(R.id.text_view_start_date)
        val distance: TextView = itemView.findViewById(R.id.text_view_distance)
        val duration: TextView = itemView.findViewById(R.id.text_view_duration)
    }

    private fun timeToString(secs: Long): String {
        val hour = secs / 3600
        val min = secs / 60 % 60
        val sec = secs / 1 % 60
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

    private fun formatDistance(distance: Int): String {
        val result = distance.toDouble() / 1000
        return result.toString()
    }
}