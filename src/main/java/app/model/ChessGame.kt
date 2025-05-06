package app.model

import app.stockfish.Stockfish

import java.awt.Color
import kotlin.math.max

const val squares: String = "abcdefgh"

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

    private var gameMode: GameMode = GameMode.PlayerVsPlayer

    private val moves: MutableList<String> = mutableListOf()
    private val stockfish: Stockfish = Stockfish("./stockfish-avx2")

    init {
        positions[currentPosition] = 1
    }

    fun makeMove(move: String, ai: Boolean = false) {
        // Dont allow user move if playing against stockfish
        if (turnColor == Color.BLACK && !ai && gameMode == GameMode.PlayerVsStockfish) {
            return
        }

        if (gameState != GameState.RUNNING) {
            return
        }

        if (newPos == null) {
            newPos = Position(currentPosition, lastMove)
        }

        val movePair = fromLongAlgebraicNotation(move);
        val from = movePair.first;
        val to = movePair.second;

        if (!newPos!!.checkValidMove(from, to)) {
            println("Invalid move: $from -> $to")
            return
        }

        val pawnMoved = newPos?.getChessBoard()!![from.first][from.second]?.type == Piece.PAWN
        val pieceCaptured = newPos?.getChessBoard()!![to.first][to.second] != null

        if (pawnMoved) {
            lastPawnMove = moveCounter
        }

        if (pieceCaptured) {
            lastCapture = moveCounter
        }

        // Model
        println("Valid move: $from -> $to")
        newPos!!.makeMove(move)
        println("From: $from -> $to")
        currentPosition = newPos!!
        newPos = null
        lastMove = Pair(from, to)
        updateCounterAndColor()
        addAndCheckRepetition()
        checkForCheckMate()
        checkFiftyMoveRule()

        // Stockfish
        if (gameMode == GameMode.PlayerVsStockfish) {
            moves.add(move)
            if (turnColor == Color.BLACK) {
                val move = stockfish.getMove(moves.joinToString(separator = " "))
                if (move == "0000") {
                    return
                }
                val nextMove = move
                this.makeMove(move, true)
            }
        }
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

    private fun toLongAlgebricNotation(from: Pair<Int, Int>, to: Pair<Int, Int>): String {
        var src = squares[from.second] + (8 - from.first).toString()
        var dest = squares[to.second] + (8 - to.first).toString()
        return "$src$dest"
    }
    
    private fun fromLongAlgebraicNotation(move: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val fromCol = squares.indexOf(move[0])
        val fromRow = 8 - move[1].toString().toInt()
        val from = Pair(fromRow, fromCol)
        val toCol = squares.indexOf(move[2])
        val toRow = 8 - move[3].toString().toInt()
        val to = Pair(toRow, toCol)

        println("From: $from -> $to")

        return Pair(from, to)
    }


    fun createNewGame(mode: GameMode) {
        gameMode = mode
        
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
