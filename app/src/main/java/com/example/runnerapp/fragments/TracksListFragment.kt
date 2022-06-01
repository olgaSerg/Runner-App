package com.example.runnerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.adapters.TracksListAdapter
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TracksListFragment : Fragment(R.layout.fragment_tracks_list) {

    private var tracksRecyclerView: RecyclerView? = null
    private var fab: FloatingActionButton? = null
    private var fabClickListener: OnFABClickListener? = null
    private var recyclerViewTrackItemClickListener: OnTracksRecyclerViewItemClickListener? = null

    interface OnFABClickListener {
        fun onFABClick()
    }

    interface OnTracksRecyclerViewItemClickListener {
        fun onTrackClick(track: TrackModel)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fabClickListener = try {
            activity as OnFABClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnFABClickListener")
        }

        recyclerViewTrackItemClickListener = try {
            activity as OnTracksRecyclerViewItemClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnTracksRecyclerViewItemClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracksRecyclerView = view.findViewById(R.id.recycler_view_tracks)
        fab = view.findViewById(R.id.floating_action_button)

        val fab = fab ?: return

        fab.setOnClickListener {
            fabClickListener?.onFABClick()
        }
    }

    override fun onResume() {
        super.onResume()
        displayTracksList()
    }

    private fun displayTracksList() {
        val db = App.instance?.db ?: return
        val tracksRecyclerView = tracksRecyclerView ?: return
        val tracksProvider = GetTracksProvider()
        tracksProvider.getTracksAsync(db).onSuccess({
            val tracks = it.result
            tracksRecyclerView.layoutManager = LinearLayoutManager(activity)
            tracksRecyclerView.adapter =
                TracksListAdapter(tracks, recyclerViewTrackItemClickListener!!)
        }, Task.UI_THREAD_EXECUTOR)
    }
}