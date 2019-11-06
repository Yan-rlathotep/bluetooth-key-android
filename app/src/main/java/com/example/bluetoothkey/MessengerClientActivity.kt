package com.example.bluetoothkey

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_messengerclient.*


class MessengerClientActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messengerclient)
        btn_startService.setOnClickListener {startService()}
        btn_stopService.setOnClickListener {stopService()}
        btn_bindToService.setOnClickListener {bindToService()}
        btn_unbindFromService.setOnClickListener {unbindFromService()}
        btn_sendOneWayMessage.setOnClickListener {sendOneWayMessage("Hello !")}
    }

    companion object {
        private const val SERVICE_PACKAGE_NAME = "com.example.bluetoothkey"
        private const val SERVICE_CLASS_NAME =
            "$SERVICE_PACKAGE_NAME.MessengerService"

        val serviceIntent: Intent
            get() {
                return Intent()
                    .setComponent(
                        ComponentName(SERVICE_PACKAGE_NAME, SERVICE_CLASS_NAME)
                    )
            }
    }

    private var serviceStarted: Boolean = false

    private var boundToService: Boolean = false

    private var serviceCallsMessenger: Messenger? = null


    private fun startService() {
           startService(serviceIntent)
            serviceStarted = true
    }

    private fun stopService() {
            stopService(serviceIntent)
            serviceStarted = false
    }

    private fun bindToService() {
        if (!boundToService) {
            bindService(serviceIntent, messengerServiceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun unbindFromService() {
        if (boundToService) {
            unbindService(messengerServiceConnection)
            boundToService = false
            serviceCallsMessenger = null
        }
    }

    private val messengerServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            boundToService = false
            serviceCallsMessenger = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundToService = true
            serviceCallsMessenger = Messenger(service)
        }
    }

    private fun buildRequestMessage(messageText: String) : Message {
        val message = Message.obtain(null, 1, 0, 0)
        message.data = wrapRequestMessagePayload(messageText)
        return message
    }

    private fun wrapRequestMessagePayload(messageText: String): Bundle {
        val payload = Bundle()
        payload.putString("message", messageText)
        return payload
    }

    private fun sendOneWayMessage(messageText: String) {
        if (boundToService) {
            val oneWayMessage = buildRequestMessage(messageText)
            serviceCallsMessenger?.send(oneWayMessage)
        }
    }

}
