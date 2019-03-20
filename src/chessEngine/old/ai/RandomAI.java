package chessEngine.old.ai;

import chessEngine.old.gameState.*;

public class RandomAI implements ChessAI {

    @Override
    public ChessMove getAIMove(ChessBoard board) {

        //ChessMove[] moves = board.getValidMoves();
        //return moves[new Random().nextInt(moves.length)];
        return null;
    }
}
