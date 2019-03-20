package chessEngine.old.gameState;

public enum ChessPiece {

    none(false, 0, " "),
    wKing(true, Integer.MAX_VALUE, "\u2654"),
    bKing(false, -Integer.MAX_VALUE, "\u265A"),
    wQueen(true, 9, "\u2655"),
    bQueen(false, -9, "\u265B"),
    wRook(true, 5, "\u2656"),
    bRook(false, -5, "\u265C"),
    wBish(true, 3, "\u2657"),
    bBish(false, -3, "\u265D"),
    wKnight(true, 3, "\u2658"),
    bKnight(false, -3, "\u265E"),
    wPawn(true, 1, "\u2659"),
    bPawn(false, -1, "\u265F"),
    wEpPawn(true, 0, " "),
    bEpPawn(false, 0, " ");

    public final boolean isWhite;
    public final int value;
    public final String symbol;

    ChessPiece(boolean isWhite, int value, String symbol){
        this.isWhite = isWhite;
        this.value = value;
        this.symbol = symbol;
    }
}
