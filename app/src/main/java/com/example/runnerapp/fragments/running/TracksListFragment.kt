package com.example.runnerapp.fragments.running

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.TracksSynchronizer
import com.example.runnerapp.activities.STATE
import com.example.runnerapp.adapters.TracksListAdapter
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class TracksListFragment : Fragment(R.layout.fragment_tracks_list) {

    private var tracksRecyclerView: RecyclerView? = null
    private var fab: FloatingActionButton? = null
    private var fabClickListener: OnFABClickListener? = null
    private var recyclerViewTrackItemClickListener: OnTracksRecyclerViewItemClickListener? = null
    private var pullToRefresh: SwipeRefreshLayout? = null
    private var db: SQLiteDatabase? = null
    private var progressIndicator: LinearProgressIndicator? = null
    private var state: State? = null

    interface OnFABClickListener {
        fun onFABClick()
    }

    interface OnTracksRecyclerViewItemClickListener {
        fun onTrackClick(track: TrackModel)
    }

    companion object {
        fun newInstance(state: State): TracksListFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val tracksListFragment = TracksListFragment()
            tracksListFragment.arguments = args
            return tracksListFragment
        }
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

        initializeFields(view)

        val fab = fab ?: return

        startSynchronization()

        fab.setOnClickListener {
            fabClickListener?.onFABClick()
        }

        setRefreshListener()
    }

    private fun initializeFields(view: View) {
        tracksRecyclerView = view.findViewById(R.id.recycler_view_tracks)
        fab = view.findViewById(R.id.floating_action_button)
        pullToRefresh = view.findViewById(R.id.swipe_container)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        state = arguments?.getSerializable(STATE) as State
    }

    private fun startSynchronization() {
        val progressIndicator = progressIndicator ?: return
        val db = App.instance?.db ?: return
        val state = state ?: return
        val getTracksProvider = GetTracksProvider()
        val tracksSynchronizer = TracksSynchronizer(db, requireContext())

        if (!state.isInitialSynchronizationDone) {
            getTracksProvider.getTracksAsync(db).onSuccess {
                val isFirstRun = it.result.isEmpty()
                if (isFirstRun) {
                    progressIndicator.isVisible = true
                }
                tracksSynchronizer.synchronizeTracks {
                    Task.call({
                        if (isFirstRun) {
                            progressIndicator.isVisible = false
                        }
                        state.isInitialSynchronizationDone = true
                    }, Task.UI_THREAD_EXECUTOR)
                    displayTracksList()
                }

            }
        } else {
            progressIndicator.isVisible = false
        }
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
        val pullToRefresh = pullToRefresh ?: return
        val db = App.instance?.db ?: return
        val getTracksProvider = GetTracksProvider()

        pullToRefresh.setOnRefreshListener {
            val tracksSynchronizer = TracksSynchronizer(db, this.requireContext())
            tracksSynchronizer.synchronizeTracks {
                pullToRefresh.isRefreshing = false
                getTracksProvider.getTracksAsync(db).onSuccess({
                    displayTracksList()
                }, Task.UI_THREAD_EXECUTOR)
            }
        }
    }

    override fun onDestroyView() {
        tracksRecyclerView = null
        fab = null
        fabClickListener = null
        recyclerViewTrackItemClickListener = null
        pullToRefresh = null
        db = null
        progressIndicator = null
        state = null
        super.onDestroyView()
    }
}