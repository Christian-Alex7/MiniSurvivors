package com.example.minisurvivors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val resultText: TextView = findViewById(R.id.resultText)
        val scoreText: TextView = findViewById(R.id.scoreText)
        val restartButton: Button = findViewById(R.id.restartButton)

        val score = intent.getIntExtra("score", 0)
        val win = intent.getBooleanExtra("win", false)

        resultText.text = if (win) "Você venceu!" else "Você perdeu!"
        scoreText.text = "Pontuação: $score"

        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
