package app.model

import java.awt.Color
import kotlin.math.max


class ChessGame {
    private val positions: MutableMap<Position, Int> = mutableMapOf()
    var currentPosition: Position = Position()
    var moveCounter: Int = 1
    var turnColor: Color = Color.WHITE
    var gameState: GameState = GameState.RUNNING
    private var newPos: Position? = null
    private var lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
    private var lastCapture: Int = 0
    private var lastPawnMove: Int = 0


    init {
        positions[currentPosition] = 1
    }

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        if (gameState != GameState.RUNNING) {
            return
        }

        if (newPos == null) {
            newPos = Position(currentPosition, lastMove)
        }

        if (newPos!!.checkValidMove(from, to)) {
            val pawnMoved = newPos?.getChessBoard()!![from.first][from.second]?.type == Piece.PAWN
            val pieceCaptured = newPos?.getChessBoard()!![to.first][to.second] != null

            if (pawnMoved) {
                lastPawnMove = moveCounter
            }

            if (pieceCaptured) {
                lastCapture = moveCounter
            }

            println("Valid move: $from -> $to")
            newPos!!.makeMove(from, to)
            println("From: $from -> $to")
            currentPosition = newPos!!
            newPos = null
            lastMove = Pair(from, to)
            updateCounterAndColor()
            addAndCheckRepetition()
            checkForCheckMate()
            checkFiftyMoveRule()
            return
        }

        println("Invalid move: $from -> $to")
    }

    private fun checkForCheckMate() {
        val pos = Position(currentPosition, lastMove = null)
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

    private fun checkFiftyMoveRule() {
        if (moveCounter - max(lastPawnMove, lastCapture) >= 50) {
            gameState = GameState.DRAW
        }
    }

    fun createNewGame() {
        positions.clear()
        currentPosition = Position()
        newPos = null
        lastMove = null
        positions[currentPosition] = 1
        moveCounter = 1
        turnColor = Color.WHITE
        gameState = GameState.RUNNING
        lastCapture = 0
        lastPawnMove = 0
    }
}