package app.model

import java.awt.Color


class ChessGame {
    private val positions: MutableMap<Position, Int> = emptyMap<Position, Int>().toMutableMap()
    var currentPosition: Position = Position()
    var moveCounter: Int = 1
    var turnColor: Color = Color.WHITE
    var gameState: GameState = GameState.RUNNING


    init {
        positions[currentPosition] = 1
    }

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        if (gameState != GameState.RUNNING) {
            return
        }

        val newPos = Position(currentPosition)
        newPos.makeMove(from, to)
        println("From: $from -> $to")
        currentPosition = newPos
        updateCounterAndColor()
        addAndCheckRepetition()
    }

    private fun updateCounterAndColor() {
        if (turnColor == Color.BLACK) {
            moveCounter++
            turnColor = Color.WHITE
        } else {
            turnColor = Color.BLACK
        }
    }

    private fun addAndCheckRepetition() {
        val old = positions.getOrDefault(currentPosition, 0)
        positions[currentPosition] = old + 1
        println("New repetition counter: ${positions[currentPosition]}")
        if (positions[currentPosition] == 3) {
            gameState = GameState.DRAW
        }
        println("Gamestate: $gameState")
    }

    fun createNewGame() {
        positions.clear()
        currentPosition = Position()
        positions[currentPosition] = 1
        moveCounter = 1
        turnColor = Color.WHITE
        gameState = GameState.RUNNING
    }
}