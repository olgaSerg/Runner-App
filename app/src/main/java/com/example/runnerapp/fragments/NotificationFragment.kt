package com.example.runnerapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat.getLongDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.models.NotificationModel
import com.example.runnerapp.providers.NotificationProvider
import java.util.Date
import java.util.Calendar

const val CHANNEL_ID = "channelID"
const val NOTIFICATION_ID = 1
const val NOTIFICATION = "notification"

class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private var timePicker: TimePicker? = null
    private var datePicker: DatePicker? = null
    private var addNotificationListener: OnButtonAddNotificationClick? = null
    private var positiveButtonClick: OnPositiveButtonClick? = null
    private var buttonAddNotification: Button? = null
    private var buttonDelete: Button? = null
    private var buttonSave: Button? = null

    interface OnButtonAddNotificationClick {
        fun addNotification(time: Long)
    }

    interface OnPositiveButtonClick {
        fun clickPositiveButton()
    }

    companion object {
        fun newInstance(notification: NotificationModel): NotificationFragment {
            val args = Bundle()
            args.putSerializable(NOTIFICATION, notification)
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

        positiveButtonClick = try {
            activity as OnPositiveButtonClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnPositiveButtonClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timePicker = view.findViewById(R.id.time_picker)
        datePicker = view.findViewById(R.id.date_picker)
        buttonDelete = view.findViewById(R.id.button_delete_notification)
        buttonSave = view.findViewById(R.id.button_save_changes)
        buttonAddNotification = view.findViewById(R.id.button_add_notification)

        val timePicker = timePicker ?: return
        val datePicker = datePicker ?: return
        val buttonDelete = buttonDelete ?: return
        val buttonSave = buttonSave ?: return
        val buttonAddNotification = buttonAddNotification ?: return
        val db = App.instance?.db ?: return

        if (arguments != null) {
            val notification = arguments?.getSerializable(NOTIFICATION) as NotificationModel
            val dataTime = notification.dataTime

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
            buttonDelete.isEnabled = true
            buttonSave.isEnabled = true
            buttonAddNotification.isEnabled = false
            buttonDelete.setOnClickListener {
                val notificationId = notification.id
                val notificationProvider = NotificationProvider()
                if (notificationId != null) {
                    notificationProvider.deleteNotification(db, notificationId)
                }
                AlertDialog.Builder(requireContext())
                    .setMessage(
                        getString(R.string.notification_delete)
                    )
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> positiveButtonClick?.clickPositiveButton() }
                    .show()
            }


            buttonSave.setOnClickListener {
                val title = "Время напоминания изменено на:"
                val time = getTime(timePicker, datePicker)
                notification.dataTime = time
                showAlert(time, title)
                val notificationProvider = NotificationProvider()
                notificationProvider.changeNotification(db, notification)
            }

        } else {
            buttonDelete.isEnabled = false
            buttonSave.isEnabled = false
            buttonAddNotification.isEnabled = true
            buttonAddNotification.setOnClickListener {
                val time = getTime(timePicker, datePicker)
                val notification = NotificationModel(null, time)
                addNotificationListener?.addNotification(time)
                val title = getString(R.string.set_notification)
                showAlert(time, title)
                val notificationProvider = NotificationProvider()
                notificationProvider.recordNotification(db, notification)
            }
        }
    }


    private fun showAlert(time: Long, title: String) {
        val date = Date(time)
        val dateFormat = getLongDateFormat(requireContext())
        val timeFormat = getTimeFormat(requireContext())

        AlertDialog.Builder(requireContext())
            .setMessage(
                title +
                        "\n" + timeFormat.format(date) + " " + dateFormat.format(date)
            )
            .setPositiveButton(getString(R.string.ok)) { _, _ -> positiveButtonClick?.clickPositiveButton() }
            .show()
    }

    private fun getTime(timePicker: TimePicker, datePicker: DatePicker): Long {

        val minute = timePicker.minute
        val hour = timePicker.hour
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }
}