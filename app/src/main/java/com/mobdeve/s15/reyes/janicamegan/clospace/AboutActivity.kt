package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        toolbarTitle = findViewById(R.id.tvToolbarTitle)
        backButton = findViewById(R.id.btnBack)

        toolbarTitle.text = getString(R.string.about)

        backButton.setOnClickListener {
            finish()
        }
    }
}