package com.example.bluetoothkey

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.*

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

    private fun startForeground() {
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

    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    inner class IncomingHandler(private val applicationContext: Context) : Handler() {
        private val payloadKeyMessage = "message"

        private fun parseRequestMessagePayload(payload: Bundle?): String? {
            if (payload != null && payload.containsKey(payloadKeyMessage)) {
                return payload.getString(payloadKeyMessage)
            } else {
                throw RuntimeException("Payload of message request is missing")
            }
        }

        fun buildResponseMessage(reply: String) : Message {
            val message = Message.obtain(null, 5, 0, 0)
            message.data = wrapResponseMessagePayload(reply)
            return message
        }

        private fun wrapResponseMessagePayload(reply: String): Bundle {
            val payload = Bundle()
            payload.putString("reply", reply)
            return payload
        }

        override fun handleMessage(msg: Message?) {
            when(msg?.what) {

                1 -> {startForeground()}

                2 -> {stopForeground()}

                3 -> {
                    val incomingMessage = parseRequestMessagePayload(msg.data)
                    Sandwich.serve(applicationContext, "Client said something", incomingMessage)
                }

                4 -> {
                    val incomingMessage = parseRequestMessagePayload(msg.data)
                    Sandwich.serve(applicationContext, "Client said something", incomingMessage)

                    val reply = buildResponseMessage("I'm fine, thank you !")
                    msg.replyTo.send(reply)
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
