package com.example.runnerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.runnerapp.fragments.RemindersFragment
import com.example.runnerapp.fragments.SelectedTrackFragment
import com.example.runnerapp.fragments.TracksListFragment
import com.example.runnerapp.models.NavigationDrawerItem
import com.example.runnerapp.models.TrackModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainScreenActivity : AppCompatActivity(), TracksListFragment.OnFABClickListener,
    TracksListFragment.OnTracksRecyclerViewItemClickListener {

    private var toolbar: MaterialToolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var itemLogout: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        initializeFields()
        loadTracksListFragment()

        val toolbar = toolbar ?: return
        val drawerLayout = drawerLayout ?: return
        val itemLogout = itemLogout ?: return

        val itemsNavigation = createItemsNavigationArray()

        val dbHelper = DBHelper(this)
        dbHelper.readableDatabase

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        sendClickPosition(itemsNavigation[0])

        itemLogout.setOnClickListener {
            itemLogout.setBackgroundColor(getColor(R.color.track_color))

            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setNavigationListener(drawerLayout, itemsNavigation)
    }

    private fun initializeFields() {
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar_main)
        drawerLayout = findViewById(R.id.drawer_layout)
        itemLogout = findViewById(R.id.item_view_logout)
    }

    private fun loadTracksListFragment() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            replace(R.id.fragment_container, TracksListFragment())
            commit()
        }
    }

    private fun setNavigationListener(
        drawerLayout: DrawerLayout,
        itemsNavigation: ArrayList<NavigationDrawerItem>
    ) {
        val navigationView = navigationView ?: return
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_screen_item -> {
                    sendClickPosition(itemsNavigation[0])
                }
                R.id.notification_item -> {
                    sendClickPosition(itemsNavigation[1])
                }
            }
            menuItem.isChecked = true
            drawerLayout.close()
            true
        }
    }

    override fun onFABClick() {
        val intent = Intent(this, RunningActivity::class.java)
        startActivity(intent)
    }

    private fun createItemsNavigationArray(): ArrayList<NavigationDrawerItem> {
        return arrayListOf(
            NavigationDrawerItem(
                getString(R.string.main_screen),
                TracksListFragment()
            ),
            NavigationDrawerItem(
                getString(R.string.reminders),
                RemindersFragment()
            )
        )
    }

    private fun sendClickPosition(selectedNavItem: NavigationDrawerItem) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTrackClick(track: TrackModel) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SelectedTrackFragment.newInstance(track.id!!))
            addToBackStack("SelectedTrack")
            commit()
        }
    }
}