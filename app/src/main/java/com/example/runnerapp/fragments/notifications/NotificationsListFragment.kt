package com.example.runnerapp.fragments.notifications

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.adapters.NotificationsListAdapter
import com.example.runnerapp.models.NotificationModel
import com.example.runnerapp.providers.NotificationProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotificationsListFragment : Fragment(R.layout.fragment_notifications_list) {
    private var notificationsRecyclerView: RecyclerView? = null
    private var fabNotification: FloatingActionButton? = null
    private var fabNotificationClickListener: OnFabNotificationClickListener? = null
    private var notificationItemClickListener: OnNotificationItemClickListener? = null

    interface OnFabNotificationClickListener {
        fun onFabNotificationClick()
    }

    interface OnNotificationItemClickListener {
        fun onNotificationItemClick(notification: NotificationModel)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        fabNotificationClickListener = try {
            activity as OnFabNotificationClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnFabNotificationClickListener")
        }

        notificationItemClickListener = try {
            activity as OnNotificationItemClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnNotificationItemClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsRecyclerView = view.findViewById(R.id.recycler_view_notifications)
        fabNotification = view.findViewById(R.id.floating_action_button_notification)

        val fabNotification = fabNotification ?: return

        fabNotification.setOnClickListener {
            fabNotificationClickListener?.onFabNotificationClick()
        }
    }

    override fun onResume() {
        super.onResume()
        displayNotificationsList()
    }

    private fun displayNotificationsList() {
        val db = App.instance?.db ?: return
        val notificationsRecyclerView = notificationsRecyclerView ?: return

        val notificationProvider = NotificationProvider()
        notificationProvider.getNotificationsAsync(db).onSuccess({
            val notifications: ArrayList<NotificationModel> = it.result
            notificationsRecyclerView.layoutManager = LinearLayoutManager(activity)
            notificationsRecyclerView.adapter =
                notificationItemClickListener?.let { notification ->
                    NotificationsListAdapter(notifications,
                        notification
                    )
                }
        }, Task.UI_THREAD_EXECUTOR)
    }

    override fun onDestroyView() {
        notificationsRecyclerView = null
        fabNotification = null
        fabNotificationClickListener = null
        notificationItemClickListener = null
        super.onDestroyView()
    }
}