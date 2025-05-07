package app.model

interface GameMode {
    fun getMode(): Mode
}

enum class Mode {
    PvP,
    PvSF,
    SFvSF,
}

public object PlayerVsPlayer: GameMode {
    override fun getMode() = Mode.PvP
}

public class PlayerVsStockfish(val rating: Int): GameMode {
    override fun getMode() = Mode.PvSF
}
