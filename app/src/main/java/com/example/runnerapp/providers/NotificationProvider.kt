package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.NotificationModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NotificationProvider {
    private val uid = Firebase.auth.uid

    fun recordNotification(
        db: SQLiteDatabase,
        notification: NotificationModel
    ): Task<NotificationModel> {
        val date = notification.dataTime
        val args = arrayOf(date, uid)

        return Task.callInBackground {
            db.execSQL(
                """INSERT INTO "notification" (date_time, uid)
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
            val args = arrayOf(uid)
            val cursor = db.rawQuery(
                """SELECT id, date_time FROM notification WHERE uid = ? ORDER BY date_time DESC""",
                args
            )
            val notifications = arrayListOf<NotificationModel>()
            with(cursor) {
                while (moveToNext()) {
                    val notification = NotificationModel()
                    notification.id = getInt(getColumnIndexOrThrow("id"))
                    notification.dataTime = getLong(getColumnIndexOrThrow("date_time"))
                    notifications.add(notification)
                }
            }
            cursor.close()
            notifications
        }
    }

    fun changeNotification(db: SQLiteDatabase, notification: NotificationModel) {
        val date = notification.dataTime
        val id = notification.id
        val args = arrayOf(date, uid, id)

        Task.callInBackground {
            db.execSQL(
                """UPDATE notification SET date_time = ? WHERE uid = ? AND id = ?
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