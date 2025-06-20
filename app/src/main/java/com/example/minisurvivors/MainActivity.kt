package com.example.minisurvivors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        val recordText = findViewById<TextView>(R.id.recordText)

        val prefs = getSharedPreferences("jogoPrefs", MODE_PRIVATE)
        val recorde = prefs.getInt("recorde", 0)
        recordText.text = "Recorde: $recorde"

        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
