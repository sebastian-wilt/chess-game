package app.controller

import app.model.ChessGame
import app.model.GameMode
import app.view.ChessView

class ChessApp {
    private val chessGame: ChessGame = ChessGame()

    init {
        ChessView(this)
    }

    fun getCurrentPosition() = chessGame.currentPosition

    fun getTurnColor() = chessGame.turnColor

    fun getMoveCounter() = chessGame.moveCounter

    fun getGameState() = chessGame.gameState

    fun makeMove(move: String) = chessGame.makeMove(move)

    fun createNewGame(mode: GameMode) = chessGame.createNewGame(mode)
}
