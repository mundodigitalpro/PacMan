package com.josejordan.pacman

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.Random
import kotlin.math.abs
import kotlin.math.min

class GameView(context: Context) : SurfaceView(context), Runnable {

    private var running = false
    private val thread: Thread = Thread(this)
    private val paint: Paint = Paint()
    private val gridSize = Point(30, 31)
    private val random = Random()
    private var cellSize: Float = 0.0f
    private var pacManDirection = Point(0, 0)
    private var offsetX: Float = 0.0f
    private var offsetY: Float = 0.0f // Aquí se declara la variable offsetY

    // Añadir Pac-Man y un fantasma
    private val pacMan = PacMan(Point(14, 15))
    private val ghost = Ghost(Point(14, 10), Color.RED)
    private val maze = Maze() // Añadir instancia del laberinto


    init {
        holder.apply {
            addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    running = true
                    thread.start()
                }
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                }
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    pause()
                }
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Verificar si el usuario ha tocado la pantalla
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y
            val pacManCenterX = pacMan.position.x.toFloat() * cellSize + cellSize / 2 + offsetX
            val pacManCenterY = pacMan.position.y.toFloat() * cellSize + cellSize / 2 + offsetY

            // Calcular la diferencia en x y y entre el toque del usuario y la posición actual de Pacman
            val deltaX = touchX - pacManCenterX
            val deltaY = touchY - pacManCenterY

            // Verificar si el usuario ha tocado cerca de Pacman y actualizar la dirección en consecuencia
            if (abs(deltaX) > abs(deltaY)) {
                if (deltaX > 0) {
                    pacManDirection = Point(1, 0)
                } else {
                    pacManDirection = Point(-1, 0)
                }
            } else {
                if (deltaY > 0) {
                    pacManDirection = Point(0, 1)
                } else {
                    pacManDirection = Point(0, -1)
                }
            }
        }
        return true
    }

    override fun run() {
        while (running) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        // Verificar si se ha actualizado la dirección de Pacman
        if (pacManDirection.x != 0 || pacManDirection.y != 0) {
            val newDirection = Point(pacManDirection.x, pacManDirection.y)
            if (pacMan.canChangeDirection(newDirection, maze)) {
                if (newDirection != pacMan.direction) {
                    pacMan.direction = newDirection
                }
            }
        }
        val newGhostDirection = randomDirection()
        if (ghost.canChangeDirection(newGhostDirection, maze)) {
            ghost.direction = newGhostDirection
        }

        pacMan.update(maze)
        ghost.update(maze)
    }




    private fun randomDirection(): Point {
        val directions = listOf(
            Point(1, 0),
            Point(-1, 0),
            Point(0, 1),
            Point(0, -1)
        )
        return directions[random.nextInt(directions.size)]
    }
    private fun draw() {
        val canvas: Canvas = holder.lockCanvas() ?: return
        canvas.drawColor(Color.BLACK)

        // Calcular el tamaño de la celda en función del tamaño de la pantalla y la relación de aspecto del laberinto
        val cellSizeWidth = width.toFloat() / gridSize.x
        val cellSizeHeight = height.toFloat() / gridSize.y
        cellSize = min(cellSizeWidth, cellSizeHeight)


        // Calcular el desplazamiento para centrar el laberinto en la pantalla
        offsetX = (width - (cellSize * gridSize.x)) / 2
        offsetY = (height - (cellSize * gridSize.y)) / 2

        // Dibujar el laberinto, Pac-Man y el fantasma
        canvas.save()
        canvas.translate(offsetX, offsetY)
        maze.draw(canvas, paint, cellSize)
        pacMan.draw(canvas, paint, cellSize)
        ghost.draw(canvas, paint, cellSize)
        canvas.restore()

        holder.unlockCanvasAndPost(canvas)
    }



    private fun control() {
        Thread.sleep(200) // Controlar la velocidad de actualización del juego
    }

    fun pause() {
        running = false
        thread.join()
    }

    fun resume() {
        running = true
        thread.start()
    }
}
class PacMan(val position: Point, var direction: Point = Point(0, 0)) {
    fun update(maze: Maze) {
        val newX = position.x + direction.x
        val newY = position.y + direction.y

        if (newX in 0 until maze.columnCount && newY in 0 until maze.rowCount && maze.data[newY][newX] == 0) {
            position.x = newX
            position.y = newY
        }
    }
    fun draw(canvas: Canvas, paint: Paint, cellSize: Float) {
        paint.color = Color.YELLOW
        canvas.drawCircle(
            position.x * cellSize + cellSize / 2,
            position.y * cellSize + cellSize / 2,
            cellSize / 2,
            paint
        )
    }
    fun canChangeDirection(newDirection: Point, maze: Maze): Boolean {
        val newX = position.x + newDirection.x
        val newY = position.y + newDirection.y
        return newX in 0 until maze.columnCount && newY in 0 until maze.rowCount && maze.data[newY][newX] == 0
    }
}
class Ghost(val position: Point, val color: Int, var direction: Point = Point(0, 0)) {
    fun update(maze: Maze) {
        val newX = position.x + direction.x
        val newY = position.y + direction.y

        if (newX in 0 until maze.columnCount && newY in 0 until maze.rowCount && maze.data[newY][newX] == 0) {
            position.x = newX
            position.y = newY
        }
    }
    fun draw(canvas: Canvas, paint: Paint, cellSize: Float) {
        paint.color = color
        canvas.drawCircle(
            position.x * cellSize + cellSize / 2,
            position.y * cellSize + cellSize / 2,
            cellSize / 2,
            paint
        )
    }
    fun canChangeDirection(newDirection: Point, maze: Maze): Boolean {
        val newX = position.x + newDirection.x
        val newY = position.y + newDirection.y

        return newX in 0 until maze.columnCount && newY in 0 until maze.rowCount && maze.data[newY][newX] == 0
    }

}
class Maze {
    val data = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 1),
        intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )



    val columnCount: Int
        get() = data[0].size

    val rowCount: Int
        get() = data.size

    fun draw(canvas: Canvas, paint: Paint, cellSize: Float) {
        for (y in data.indices) {
            for (x in data[y].indices) {
                if (data[y][x] == 1) {
                    paint.color = Color.BLUE
                    canvas.drawRect(
                        x * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                        paint
                    )
                }
            }
        }
    }
}

