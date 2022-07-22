package com.example.runnerapp.fragments.notifications

import android.app.AlertDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.format.DateFormat.getLongDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.activities.STATE
import com.example.runnerapp.models.NotificationModel
import com.example.runnerapp.providers.NotificationProvider
import java.util.Date
import java.util.Calendar

const val CHANNEL_ID = "channelID"

class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private var timePicker: TimePicker? = null
    private var datePicker: DatePicker? = null
    private var addNotificationListener: OnButtonAddNotificationClick? = null
    private var loadNotificationsListListener: LoadNotificationListListener? = null
    private var addNotificationButton: Button? = null
    private var deleteButton: Button? = null
    private var saveButton: Button? = null
    private var state: State? = null
    private var deleteNotificationClick: OnDeleteNotificationClick? = null

    interface OnButtonAddNotificationClick {
        fun addNotification(notification: NotificationModel)
    }

    interface LoadNotificationListListener {
        fun loadNotificationsList()
    }

    interface OnDeleteNotificationClick {
        fun deleteNotification(notification: NotificationModel)
    }

    companion object {
        fun newInstance(state: State): NotificationFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val notificationFragment = NotificationFragment()
            notificationFragment.arguments = args
            return notificationFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        addNotificationListener = try {
            activity as OnButtonAddNotificationClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonAddNotificationClick")
        }

        loadNotificationsListListener = try {
            activity as LoadNotificationListListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnPositiveButtonClick")
        }

        deleteNotificationClick = try {
            activity as OnDeleteNotificationClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnDeleteNotificationClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFields(view)

        state = arguments?.getSerializable(STATE) as State
        val state = state ?: return
        val db = App.instance?.db ?: return

        if (state.notification != null) {
            displayState()
        } else {
            state.notification = NotificationModel()
            deleteButton?.isEnabled = false
            saveButton?.isEnabled = false
        }

        setAddNotificationButtonClickListener(db)
        setDeleteButtonClickListener(db)
        setSaveButtonClickListener(db)
    }

    private fun initializeFields(view: View) {
        timePicker = view.findViewById(R.id.time_picker)
        datePicker = view.findViewById(R.id.date_picker)
        deleteButton = view.findViewById(R.id.button_delete_notification)
        saveButton = view.findViewById(R.id.button_save_changes)
        addNotificationButton = view.findViewById(R.id.button_add_notification)
    }

    private fun displayState() {
        val deleteButton = deleteButton ?: return
        val saveButton = saveButton ?: return
        val addNotificationButton = addNotificationButton ?: return
        val state = state ?: return
        val timePicker = timePicker ?: return
        val datePicker = datePicker ?: return

        if (!state.isNewNotification) {
            deleteButton.isEnabled = true
            saveButton.isEnabled = true
            addNotificationButton.isEnabled = false
        } else {
            deleteButton.isEnabled = false
            saveButton.isEnabled = false
            addNotificationButton.isEnabled = true
        }

        val dataTime = state.notification?.notifyAt

        val date = Date(dataTime!!)
        val calendar = Calendar.getInstance()
        calendar.time = date
        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)
        datePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun setAddNotificationButtonClickListener(db: SQLiteDatabase) {
        val timePicker = timePicker ?: return
        val datePicker = datePicker ?: return
        val addNotificationButton = addNotificationButton ?: return
        addNotificationButton.setOnClickListener {
            val time = getTimeInMillis(timePicker, datePicker)
            val notification = NotificationModel(null, time)
            val notificationProvider = NotificationProvider()
            notificationProvider.recordNotification(db, notification).onSuccess({
                addNotificationListener?.addNotification(it.result)
                val title = getString(R.string.set_notification)
                showAlert(time, title) {
                    loadNotificationsListListener?.loadNotificationsList()
                }
            }, Task.UI_THREAD_EXECUTOR)
        }
    }

    private fun setDeleteButtonClickListener(db: SQLiteDatabase) {
        val deleteButton = deleteButton ?: return
        val notification = state?.notification ?: return
        deleteButton.setOnClickListener {
            val notificationId = notification.id
            val notificationProvider = NotificationProvider()
            if (notificationId != null) {
                notificationProvider.deleteNotification(db, notificationId).onSuccess({
                    deleteNotificationClick?.deleteNotification(notification)
                    AlertDialog.Builder(requireContext())
                        .setMessage(
                            getString(R.string.notification_delete)
                        )
                        .setPositiveButton(getString(R.string.ok)) { _, _ -> loadNotificationsListListener?.loadNotificationsList() }
                        .show()
                }, Task.UI_THREAD_EXECUTOR)
            }
        }
    }

    private fun setSaveButtonClickListener(db: SQLiteDatabase) {
        val timePicker = timePicker ?: return
        val datePicker = datePicker ?: return
        val saveButton = saveButton ?: return
        val notification = state?.notification ?: return
        saveButton.setOnClickListener {
            val title = getString(R.string.time_change_notification)
            val time = getTimeInMillis(timePicker, datePicker)
            val notificationProvider = NotificationProvider()
            notification.notifyAt = time
            notificationProvider.changeNotification(db, notification).onSuccess ({
                addNotificationListener?.addNotification(notification)
                showAlert(time, title) {
                    loadNotificationsListListener?.loadNotificationsList()
                }
            }, Task.UI_THREAD_EXECUTOR)
        }
    }

    private fun showAlert(time: Long, title: String, okCallback: () -> Unit) {
        val date = Date(time)
        val dateFormat = getLongDateFormat(requireContext())
        val timeFormat = getTimeFormat(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(
            """
                ${timeFormat.format(date)} ${dateFormat.format(date)}
            """.trimIndent()
        )
            .setPositiveButton(getString(R.string.ok)) { _, _ -> okCallback()}
            .show()
    }

    private fun getTimeInMillis(timePicker: TimePicker, datePicker: DatePicker): Long {

        val minute = timePicker.minute
        val hour = timePicker.hour
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    override fun onPause() {
        super.onPause()
        if (timePicker != null && datePicker != null) {
            val time = getTimeInMillis(timePicker!!, datePicker!!)
            state?.notification?.notifyAt = time
        }
    }

    override fun onDestroyView() {
        timePicker = null
        datePicker = null
        addNotificationListener = null
        loadNotificationsListListener = null
        addNotificationButton = null
        deleteButton = null
        saveButton = null
        state = null
        deleteNotificationClick = null
        super.onDestroyView()
    }
}