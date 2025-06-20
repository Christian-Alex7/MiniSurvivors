package com.example.minisurvivors

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private val playerSize = 120f
    private val enemySize = 120f
    private val bulletSize = 64f

    private val playerBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.player),
        playerSize.toInt(), playerSize.toInt(), false
    )
    private val enemyBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.enemy),
        enemySize.toInt(), enemySize.toInt(), false
    )
    private val bulletBitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.bullet),
        bulletSize.toInt(), bulletSize.toInt(), false
    )
    private val background: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.background),
        resources.displayMetrics.widthPixels,
        resources.displayMetrics.heightPixels,
        false
    )

    private var playerX = 100f
    private var playerY = 0f
    private var playerSpeed = 18f
    private var moveX = 0f
    private var moveY = 0f
    private var life = 1
    private var score = 0
    private var time = 60
    private var gameEnded = false

    private val enemies = mutableListOf<Pair<Float, Float>>()
    private val bullets = mutableListOf<Pair<Float, Float>>()
    private val paint = Paint()

    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            if (!gameEnded) {
                update()
                invalidate()
                handler.postDelayed(this, 30)
            }
        }
    }

    private val timerHandler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!gameEnded) {
                time--
                if (time <= 0) {
                    endGame(true)
                } else {
                    timerHandler.postDelayed(this, 1000)
                }
            }
        }
    }

    init {
        playerY = resources.displayMetrics.heightPixels - 200f
        handler.post(runnable)
        timerHandler.post(timerRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(background, 0f, 0f, paint)
        canvas.drawBitmap(playerBitmap, playerX, playerY, paint)

        for ((x, y) in enemies) {
            canvas.drawBitmap(enemyBitmap, x, y, paint)
        }

        for ((x, y) in bullets) {
            canvas.drawBitmap(bulletBitmap, x, y, paint)
        }

        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("Vida: $life", 20f, 50f, paint)
        canvas.drawText("Tempo: $time", 20f, 100f, paint)
        canvas.drawText("Score: $score", 20f, 150f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            moveX = event.x - (playerX + playerSize / 2)
            moveY = event.y - (playerY + playerSize / 2)
        }
        return true
    }

    private fun update() {
        // Movimento do jogador
        val dx = if (abs(moveX) > 10) playerSpeed * moveX / abs(moveX) else 0f
        val dy = if (abs(moveY) > 10) playerSpeed * moveY / abs(moveY) else 0f

        playerX += dx
        playerY += dy
        playerX = playerX.coerceIn(0f, width - playerSize)
        playerY = playerY.coerceIn(0f, height - playerSize)

        // Spawn de inimigos
        if (Random.nextInt(100) < 20) {
            val x = Random.nextFloat() * (width - enemySize)
            val y = 0f
            enemies.add(Pair(x, y))
        }

        // Movimento dos inimigos em direção ao jogador
        for (i in enemies.indices) {
            val (ex, ey) = enemies[i]
            val angle = atan2(playerY - ey, playerX - ex)
            val speed = 65f // ← AQUI: velocidade aumentada
            val newX = ex + cos(angle) * speed
            val newY = ey + sin(angle) * speed
            enemies[i] = Pair(newX, newY)
        }

        // Tiro automático
        if (score % 5 == 0) {
            bullets.add(Pair(playerX + playerSize / 2 - bulletSize / 2, playerY))
        }

        // Atualização dos tiros
        val newBullets = mutableListOf<Pair<Float, Float>>()
        for ((x, y) in bullets) {
            val newY = y - 40
            if (newY >= 0) newBullets.add(Pair(x, newY))
        }
        bullets.clear()
        bullets.addAll(newBullets)

        // Verificar colisão jogador x inimigo
        val iter = enemies.iterator()
        while (iter.hasNext()) {
            val (x, y) = iter.next()
            if (RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
                    .intersect(RectF(x, y, x + enemySize, y + enemySize))) {
                life--
                iter.remove()
                if (life <= 0) {
                    endGame(false)
                    return
                }
            }
        }

        // Verificar colisão tiro x inimigo
        val hitEnemies = mutableListOf<Pair<Float, Float>>()
        for ((bx, by) in bullets) {
            for ((ex, ey) in enemies) {
                if (RectF(bx, by, bx + bulletSize, by + bulletSize)
                        .intersect(RectF(ex, ey, ex + enemySize, ey + enemySize))) {
                    hitEnemies.add(Pair(ex, ey))
                }
            }
        }
        enemies.removeAll(hitEnemies)

        score++
    }

    private fun endGame(win: Boolean) {
        if (gameEnded) return
        gameEnded = true
        handler.removeCallbacks(runnable)
        timerHandler.removeCallbacks(timerRunnable)
        val prefs = context.getSharedPreferences("jogoPrefs", Context.MODE_PRIVATE)
        val recorde = prefs.getInt("recorde", 0)
        if (score > recorde) {
            prefs.edit().putInt("recorde", score).apply()
        }

        val intent = Intent(context, GameOverActivity::class.java)
        intent.putExtra("score", score)
        intent.putExtra("win", win)
        context.startActivity(intent)
    }

    private data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        fun intersect(other: RectF): Boolean {
            return !(left > other.right || right < other.left || top > other.bottom || bottom < other.top)
        }
    }
}
