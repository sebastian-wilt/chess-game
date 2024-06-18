package app.model

import java.awt.Color


class ChessGame {
    private val positions: MutableMap<Position, Int> = emptyMap<Position, Int>().toMutableMap()
    var currentPosition: Position = Position()
    var moveCounter: Int = 1
    var turnColor: Color = Color.WHITE
    var gameState: GameState = GameState.RUNNING
    private var newPos: Position? = null


    init {
        positions[currentPosition] = 1
    }

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        if (gameState != GameState.RUNNING) {
            return
        }

        if (newPos == null) {
            newPos = Position(currentPosition)
        }

        if (newPos!!.checkValidMove(from, to)) {
            println("Valid move: $from -> $to")
            newPos!!.makeMove(from, to)
            println("From: $from -> $to")
            currentPosition = newPos!!
            newPos = null
            updateCounterAndColor()
            addAndCheckRepetition()
            checkForCheckMate()
            return
        }

        println("Invalid move: $from -> $to")
    }

    private fun checkForCheckMate() {
        val pos = Position(currentPosition)
        if (pos.isCheck) {
            gameState = if (turnColor == Color.WHITE) GameState.BLACK else GameState.WHITE
        }
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
        newPos = null
        positions[currentPosition] = 1
        moveCounter = 1
        turnColor = Color.WHITE
        gameState = GameState.RUNNING
    }
}