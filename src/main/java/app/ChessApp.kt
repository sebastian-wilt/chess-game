package app

class ChessApp() {
    private val gui: ChessView = ChessView(this)
    private val chessGame: ChessGame = ChessGame(this)

}