package com.example.bluetoothkey

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build

class MessengerService : Service() {
    private val channelId = "MessengerServiceChannel"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun displayNotification() {
        createNotificationChannel()

        val pendingIntent: PendingIntent =
            Intent(this, MessengerClientActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle(getText(R.string.messenger_notification_title))
            .setContentText(getText(R.string.messenger_notification_message))
            .setSmallIcon(R.drawable.key)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        displayNotification()

        return super.onStartCommand(intent, flags, startId)
    }

    class IncomingHandler(private val applicationContext: Context) : Handler() {

        override fun handleMessage(msg: Message?) {
            when(msg?.what) {

                1 -> {
                    val incomingMessage = "Hey !"
                    Sandwich.serve(applicationContext, "Client said something", incomingMessage)
                }

                else -> {
                    Sandwich.serve(applicationContext, "Error", "Client sent message with unknown WHAT ${msg?.what}")
                }
            }
        }
    }

    private val messenger by lazy { Messenger(IncomingHandler(applicationContext)) }

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }
}
