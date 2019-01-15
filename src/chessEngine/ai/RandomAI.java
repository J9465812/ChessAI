package chessEngine.ai;

import chessEngine.gameState.*;

import java.util.Random;

public class RandomAI implements ChessAI {

    @Override
    public ChessMove getAIMove(ChessBoard board) {

        ChessMove[] moves = board.getValidMoves();
        return moves[new Random().nextInt(moves.length)];
    }
}
