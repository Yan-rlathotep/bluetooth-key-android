package com.example.bluetoothkey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_openMessenger.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, MessengerClientActivity::class.java)
            )
        }
    }
}
