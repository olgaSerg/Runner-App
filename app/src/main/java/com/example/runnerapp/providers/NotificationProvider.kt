package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.NotificationModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NotificationProvider {
    private val userId = Firebase.auth.uid

    fun recordNotification(
        db: SQLiteDatabase,
        notification: NotificationModel
    ): Task<NotificationModel> {
        val date = notification.notifyAt
        val args = arrayOf(date, userId)

        return Task.callInBackground {
            db.execSQL(
                """INSERT INTO "notification" (notify_at, user_id)
                    VALUES (?, ?)
                """,
                args
            )
            val cursor = db.rawQuery("""SELECT MAX(id) AS id FROM notification""", null)
            with(cursor) {
                moveToNext()
                notification.id = getInt(getColumnIndexOrThrow("id"))
            }
            cursor.close()

            notification
        }
    }

    fun getNotificationsAsync(db: SQLiteDatabase): Task<ArrayList<NotificationModel>> {
        return Task.callInBackground {
            val args = arrayOf(userId)
            val cursor = db.rawQuery(
                """SELECT id, notify_at FROM notification WHERE user_id = ? ORDER BY notify_at DESC""",
                args
            )
            val notifications = arrayListOf<NotificationModel>()
            with(cursor) {
                while (moveToNext()) {
                    val notification = NotificationModel()
                    notification.id = getInt(getColumnIndexOrThrow("id"))
                    notification.notifyAt = getLong(getColumnIndexOrThrow("notify_at"))
                    notifications.add(notification)
                }
            }
            cursor.close()
            notifications
        }
    }

    fun changeNotification(db: SQLiteDatabase, notification: NotificationModel): Task<Unit> {
        val date = notification.notifyAt
        val id = notification.id
        val args = arrayOf(date, userId, id)

        return Task.callInBackground {
            db.execSQL(
                """UPDATE notification SET notify_at = ? WHERE user_id = ? AND id = ?
                """,
                args
            )
        }
    }

    fun deleteNotification(db: SQLiteDatabase, id: Int): Task<Unit> {
        return Task.callInBackground {
            val args = arrayOf("$id")
            db.execSQL(
                """DELETE FROM notification WHERE id = ?""",
                args
            )
        }
    }
}