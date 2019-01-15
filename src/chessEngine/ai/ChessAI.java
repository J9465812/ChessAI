package chessEngine.ai;

import chessEngine.gameState.*;

public interface ChessAI {

    ChessMove getAIMove(ChessBoard board);
}
