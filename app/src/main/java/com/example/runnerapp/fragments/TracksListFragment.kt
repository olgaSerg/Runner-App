package com.example.runnerapp.fragments

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.adapters.TracksListAdapter
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TracksListFragment : Fragment(R.layout.fragment_tracks_list) {

    private var tracksRecyclerView: RecyclerView? = null
    private var fab: FloatingActionButton? = null
    private var fabClickListener: OnFABClickListener? = null
    private var recyclerViewTrackItemClickListener: OnTracksRecyclerViewItemClickListener? = null
    private var pullToRefresh: SwipeRefreshLayout? = null
    private var db: SQLiteDatabase? = null

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
        pullToRefresh = view.findViewById(R.id.swipe_container)

        val fab = fab ?: return

        fab.setOnClickListener {
            fabClickListener?.onFABClick()
        }

        setRefreshListener()
    }

    override fun onResume() {
        super.onResume()
        displayTracksList()
    }

    private fun displayTracksList() {
        db = App.instance?.db ?: return
        val tracksRecyclerView = tracksRecyclerView ?: return
        val tracksProvider = GetTracksProvider()
        tracksProvider.getTracksAsync(db!!).onSuccess({
            val tracks = it.result
            tracksRecyclerView.layoutManager = LinearLayoutManager(activity)
            tracksRecyclerView.adapter =
                TracksListAdapter(tracks, recyclerViewTrackItemClickListener!!)
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun setRefreshListener() {
        db = App.instance?.db ?: return
        val pullToRefresh = pullToRefresh ?: return
        val getTracksProvider = GetTracksProvider()
        val tracksProvider = GetTracksProvider()
        val recordTrackProvider = RecordTrackProvider()
        pullToRefresh.setOnRefreshListener {
            getTracksProvider.getTracksKeysFromFirebase { keysList ->
                Log.i("!!!keysListFirebase", keysList.joinToString(separator = " "))
                getTracksProvider.getFirebaseKeyFromDbAsync(db!!).onSuccess({
                    Log.i("!!!keysFromDB", it.result.joinToString(separator = " "))
                    val result = getNewTracksKeysFromFirebase(keysList, it.result)
                    Log.i("!!!newKeysListFirebase", result.joinToString(separator = " "))
                    result
                }, Task.BACKGROUND_EXECUTOR)
                    .onSuccess {
                        getTracksProvider.getNewTracksList(it.result) { tracksList ->
                            Log.i("newTracksListFromFirebase", tracksList.joinToString(separator = " "))
                            recordTrackProvider.recordNewTracksFromFirebase(db!!, tracksList)

                                .onSuccess {
                                    pullToRefresh.isRefreshing = false
                                    tracksProvider.getTracksAsync(db!!).onSuccess({
                                        displayTracksList()
                                        Log.i("getTracksListFromDB", it.result.joinToString(separator = " "))
                                    }, Task.UI_THREAD_EXECUTOR)
                                }
                        }
                    }
            }
        }
    }

    private fun getNewTracksKeysFromFirebase(
        keysListFirebase: ArrayList<String>,
        keysListLocalDb: ArrayList<String>
    ): ArrayList<String> {
        var isSynchronized = false
        val newKeysList = ArrayList<String>()
        for (firebaseKey in keysListFirebase) {
            for (localKey in keysListLocalDb) {
                if (firebaseKey == localKey) {
                    isSynchronized = true
                    break
                }
            }
            if (!isSynchronized) {
                newKeysList.add(firebaseKey)
            } else {
                isSynchronized = false
            }
        }
        return newKeysList
    }
}