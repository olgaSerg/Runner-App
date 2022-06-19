package com.example.runnerapp.activities


import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.runnerapp.models.NavigationDrawerItem
import com.example.runnerapp.models.NotificationModel
import com.example.runnerapp.models.TrackModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.location.LocationManager
import androidx.core.content.ContentProviderCompat.requireContext
import bolts.Task
import com.example.runnerapp.*
import com.example.runnerapp.fragments.notifications.CHANNEL_ID
import com.example.runnerapp.fragments.notifications.NotificationFragment
import com.example.runnerapp.fragments.notifications.NotificationsListFragment
import com.example.runnerapp.fragments.running.SelectedTrackFragment
import com.example.runnerapp.fragments.running.TracksListFragment

const val TRACKS_LIST = "tracks_list"
const val SELECTED_TRACK = "selected_track"
const val NOTIFICATIONS_LIST = "notifications_list"
const val NOTIFICATION = "notification"

class MainScreenActivity : AppCompatActivity(), TracksListFragment.OnFABClickListener,
    TracksListFragment.OnTracksRecyclerViewItemClickListener,
    NotificationsListFragment.OnFabNotificationClickListener,
    NotificationFragment.OnButtonAddNotificationClick,
    NotificationsListFragment.OnNotificationItemClickListener,
    NotificationFragment.LoadNotificationListListener, NotificationFragment.OnDeleteNotificationClick {

    private var toolbar: MaterialToolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var itemLogout: LinearLayout? = null
    private var locationManager: LocationManager? = null
    private var geolocationEnabled = false
    private var state = State()
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                state.fragment = TRACKS_LIST
                displayState()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    askUserOpeningAppSettings()
                }
                else {
                    askUserOpeningAppSettings()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        createNotificationChannel()


        initializeFields()

        val toolbar = toolbar ?: return
        val drawerLayout = drawerLayout ?: return

        val itemsNavigation = createItemsNavigationArray()

        if (savedInstanceState != null) {
            state = savedInstanceState.getSerializable(STATE) as State
        } else {
            state.fragment = TRACKS_LIST
        }



        showFragmentPreview()
        displayState()


        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        setItemLogoutClickListener()
        setNavigationListener(drawerLayout, itemsNavigation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(STATE, state)
    }

    private fun displayState() {
        when (state.fragment) {
            TRACKS_LIST -> loadTracksList()
            NOTIFICATION -> loadNotification()
            NOTIFICATIONS_LIST -> loadNotificationsList()
            SELECTED_TRACK -> loadSelectedTrack()
        }
    }

    private fun showFragmentPreview() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) && checkLocationServiceEnabled()
        ) {
//            displayState()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            checkLocationServiceEnabled()
        }
    }

    private fun loadSelectedTrack() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SelectedTrackFragment.newInstance(state))
            commit()
        }
    }

    private fun loadNotification() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, NotificationFragment.newInstance(state))
            commit()
        }
    }

    private fun askUserOpeningAppSettings() {
        val settingIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.request_permission))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                startActivity(settingIntent)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun initializeFields() {
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar_main)
        drawerLayout = findViewById(R.id.drawer_layout)
        itemLogout = findViewById(R.id.item_view_logout)
    }

//    private fun loadTracksListFragment() {
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.fragment_container, TracksListFragment())
//            commit()
//        }
//        state.fragment = TRACKS_LIST
//    }

    private fun checkLocationServiceEnabled(): Boolean {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            geolocationEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (geolocationEnabled) {
            return true
        } else {
            buildAlertMessageNoLocationService()
        }
        return false
    }

    private fun buildAlertMessageNoLocationService(): Boolean {
        AlertDialog.Builder(this)
            .setMessage("Необходимо включить определение местоположения")
            .setPositiveButton(
                "Включить"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setCancelable(false)
            .create()
            .show()
        return true
    }

    private fun setItemLogoutClickListener() {
        val itemLogout = itemLogout ?: return
        itemLogout.setOnClickListener {
            itemLogout.setBackgroundColor(getColor(R.color.color_checked_item))

            Firebase.auth.signOut()

            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
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
                    state.fragment = TRACKS_LIST
                }
                R.id.notification_item -> {
                    state.fragment = NOTIFICATIONS_LIST
                }
            }
            displayState()
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
                TRACKS_LIST,
                TracksListFragment(),
                getString(R.string.main_screen)
            ),
            NavigationDrawerItem(
                NOTIFICATIONS_LIST,
                NotificationsListFragment(),
                getString(R.string.reminders)
            )
        )
    }

    private fun loadTracksList() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, TracksListFragment.newInstance(state))
            commit()
        }

        if (toolbar != null) {
            toolbar!!.title = "Главный экран"
        }
        if (drawerLayout != null) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
    }

    override fun loadNotificationsList() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, NotificationsListFragment())
            commit()
        }

        if (toolbar != null) {
            toolbar!!.title = "Напоминания"
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
        state.trackId = track.id
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.fragment_container, SelectedTrackFragment.newInstance(state))
//            addToBackStack("SelectedTrack")
//            commit()
//        }
        state.fragment = SELECTED_TRACK
        displayState()
    }

    override fun onFabNotificationClick() {
        state.isNewNotification = true
        state.notification = null
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.fragment_container, NotificationFragment.newInstance(state))
//            addToBackStack("Notification")
//            commit()
//        }
        state.fragment = NOTIFICATION
        displayState()
    }

    private fun getPendingIntent(notification: NotificationModel): PendingIntent {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getBroadcast(
            applicationContext,
            notification.id!!,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun addNotification(notification: NotificationModel) {
        val pendingIntent = getPendingIntent(notification)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notification.notifyAt!!,
            pendingIntent
        )
    }

    override fun deleteNotification(notification: NotificationModel) {
        val pendingIntent = getPendingIntent(notification)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CHANNEL"
            val descriptionText = "IT'S A CHANNEL"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNotificationItemClick(notification: NotificationModel) {
        state.isNewNotification = false
        state.notification = notification
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.fragment_container, NotificationFragment.newInstance(state))
//            addToBackStack("Notification")
//            commit()
//        }
        state.fragment = NOTIFICATION
        displayState()
    }

    override fun onRestart() {
        super.onRestart()
        showFragmentPreview()
        checkLocationServiceEnabled()
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        when (state.fragment) {
////            TRACKS_LIST -> loadTracksListFragment()
//            NOTIFICATION -> loadNotificationsList()
////            NOTIFICATIONS_LIST -> loadTracksListFragment()
//            SELECTED_TRACK -> loadTracksListFragment()
//        }
//    }
}

