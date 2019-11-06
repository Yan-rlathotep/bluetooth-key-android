package com.example.bluetoothkey

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_messengerclient.*


class MessengerClientActivity: AppCompatActivity() {
    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hide(view: View) {
        view.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messengerclient)
        btn_startService.setOnClickListener {startService()}
        btn_stopService.setOnClickListener {stopService()}
        btn_bindToService.setOnClickListener {bindToService()}
        btn_unbindFromService.setOnClickListener {unbindFromService()}
        btn_sendOneWayMessage.setOnClickListener {sendOneWayMessage("Hello !")}
        btn_sendMessageWithReplyTo.setOnClickListener {sendMessageWithReplyTo("How are you ?")}
        btn_runInForeground.setOnClickListener {enableForeground()}
        btn_stopRunningInForeground.setOnClickListener {disableForeground()}
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
            boundToService = true
            show(view_serviceIsBound)
        }
    }

    private fun unbindFromService() {
        if (boundToService) {
            unbindService(messengerServiceConnection)
            boundToService = false
            hide(view_serviceIsBound)
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

    private fun enableForeground() {
        if (boundToService) {
            val oneWayMessage = buildRequestMessage(1)
            serviceCallsMessenger?.send(oneWayMessage)
        }
    }

    private fun disableForeground() {
        if (boundToService) {
            val oneWayMessage = buildRequestMessage(2)
            serviceCallsMessenger?.send(oneWayMessage)
        }
    }

    private fun buildRequestMessage(what: Int, messageText: String = "", replyTo: Messenger? = null) : Message {
        val message = Message.obtain(null, what, 0, 0)
        message.data = wrapRequestMessagePayload(messageText)
        message.replyTo = replyTo
        return message
    }

    private fun wrapRequestMessagePayload(messageText: String): Bundle {
        val payload = Bundle()
        payload.putString("message", messageText)
        return payload
    }

    private fun sendOneWayMessage(messageText: String) {
        if (boundToService) {
            val oneWayMessage = buildRequestMessage(3, messageText)
            serviceCallsMessenger?.send(oneWayMessage)
        }
    }

    private fun sendMessageWithReplyTo(messageText: String) {
        if (boundToService) {
            val messageWithReplyTo = buildRequestMessage(4, messageText, callbackMessenger)
            serviceCallsMessenger?.send(messageWithReplyTo)
        }
    }

    private val callbackMessenger by lazy { Messenger(CallbackHandler(applicationContext)) }


    private class CallbackHandler(val applicationContext: Context) : Handler() {
        private val payloadKeyReply = "reply"

        private fun parseResponseMessagePayload(payload: Bundle?): String? {
            if (payload != null && (payload.containsKey(payloadKeyReply))) {
                return payload.getString(payloadKeyReply)
            } else {
                throw RuntimeException("Payload of message request is missing")
            }
        }

        override fun handleMessage(msg: Message?) {
            when(msg?.what) {
                5 -> {
                    Thread.sleep(2_000)
                    val reply = parseResponseMessagePayload(msg.data)
                    Toast.makeText(applicationContext, "Service replied:  $reply", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(applicationContext, "Error: service returned unexpected WHAT: ${msg?.what}", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

}
