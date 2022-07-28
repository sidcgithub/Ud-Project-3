package com.udacity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val text: String =  intent.getStringExtra("Filename")!!
        Toast.makeText(this,text, Toast.LENGTH_SHORT).show()
        fileTtile.text = intent.getStringExtra("Filename")
        status.text = intent.getStringExtra("Status")
    }

}
