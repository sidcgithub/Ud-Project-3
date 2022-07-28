package com.udacity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val text: String = intent.getStringExtra("Filename")!!
        val id = intent.getIntExtra("NotifId", 0)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        fileTtile.text = intent.getStringExtra("Filename")
        status.text = intent.getStringExtra("Status")
        with(NotificationManagerCompat.from(this)) {
            cancel(id)
        }
        navigateToMain.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(mainIntent)
        }
    }

}
