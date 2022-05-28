package com.example.runnerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runnerapp.adapters.NavDrawerAdapter
import com.example.runnerapp.fragments.RemindersFragment
import com.example.runnerapp.fragments.SelectedTrackFragment
import com.example.runnerapp.fragments.TracksListFragment
import com.example.runnerapp.models.NavigationDrawerItem
import com.example.runnerapp.models.TrackModel
import com.google.android.material.appbar.MaterialToolbar

class MainScreenActivity : AppCompatActivity(), TracksListFragment.OnFABClickListener,
    NavDrawerAdapter.OnClickNavigationDrawerMenu, TracksListFragment.OnTracksRecyclerViewItemClickListener {

    private var toolbar: MaterialToolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            replace(R.id.fragment_container, TracksListFragment())
            commit()
        }

        recyclerView = findViewById(R.id.recycler_view)
        toolbar = findViewById(R.id.toolbar_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        val recyclerView = recyclerView ?: return
        val toolbar = toolbar ?: return
        val drawerLayout = drawerLayout ?: return

        val dbHelper = DBHelper(this)
        dbHelper.readableDatabase

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        val itemsNavigation = createItemsNavigationArray()
        sendClickPosition(itemsNavigation[0])

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NavDrawerAdapter(itemsNavigation)
    }

    override fun onFABClick() {
        val intent = Intent(this, RunningActivity::class.java)
        startActivity(intent)
    }

    private fun createItemsNavigationArray(): ArrayList<NavigationDrawerItem> {
        return arrayListOf(
            NavigationDrawerItem(
                getString(R.string.main_screen),
                R.drawable.ic_menu_24,
                TracksListFragment()),
            NavigationDrawerItem(
                getString(R.string.reminders),
                R.drawable.ic_reminders_24,
                RemindersFragment()
            )
        )
    }

    override fun sendClickPosition(selectedNavItem: NavigationDrawerItem) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, selectedNavItem.fragment)
            commit()
        }

        if (toolbar != null) {
            toolbar!!.title = selectedNavItem.name
        }
        if (drawerLayout != null) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
    }

    override fun onTrackClick(track: TrackModel) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SelectedTrackFragment.newInstance(track.id!!))
            addToBackStack("SelectedTrack")
            commit()
        }
    }
}