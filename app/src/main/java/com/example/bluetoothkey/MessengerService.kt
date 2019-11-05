package com.example.bluetoothkey

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class MessengerService : Service() {

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
