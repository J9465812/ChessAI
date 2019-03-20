package chessEngine.old.ai;

import chessEngine.old.gameState.ChessBoard;
import chessEngine.old.gameState.ChessMove;

public class AlphaBetaAI implements ChessAI {

    public static final float[] fileValue = {0.1f, 0.1f, 0.125f, 0.2f, 0.2f, 0.125f, 0.1f, 0.1f};
    public static final float[] rankValue = {0.1f, 0.1f, 0.1f, 0.2f, 0.2f, 0.25f, 0.25f, 0.2f};
    public static final float[] pawnValue = {0, 0.7f, 0.7f, 1.0f, 1.2f, 1.5f, 2.0f, 9.0f};

    public int depth;

    public AlphaBetaAI(int depth){
        this.depth = depth;
    }

    @Override
    public ChessMove getAIMove(ChessBoard board) {

        int[] bestMove = null;

        int[][] moveList = board.getValidMoves();

        System.out.print("Analysis: " + moveList.length + " moves: ");

        if(board.isWhiteTurn){
            float finalValue = -Float.MAX_VALUE;
            for(int[] move : moveList){
                ChessBoard potentialBoard = board.applyMoveUnsafe(move[0], move[1], move[2], move[3]);
                float value = alphaBetaSearch(potentialBoard, depth - 1, finalValue, Float.MAX_VALUE);
                if(value >= finalValue){
                    finalValue = value;
                    bestMove = move;
                }

                System.out.printf(move.toString() + " -> %+.2f  ", value);
            }
        }else{
            float finalValue = Float.MAX_VALUE;
            for(int[] move : moveList){
                ChessBoard potentialBoard = board.applyMoveUnsafe(move[0], move[1], move[2], move[3]);
                float value = alphaBetaSearch(potentialBoard, depth - 1, -Float.MAX_VALUE, finalValue);
                if(value <= finalValue){
                    finalValue = value;
                    bestMove = move;
                }

                System.out.printf(move.toString() + " -> %+.2f  ", -value);
            }
        }

        return new ChessMove(bestMove[0], bestMove[1], bestMove[2], bestMove[3]);
    }

    // Algorithm Explanation : https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning

    public float alphaBetaSearch(ChessBoard board, int depth, float alpha, float beta){

        if(depth == 0 || board.isCheckMate()){
            return simpleAnalysis(board);
        }

        int[][] moveList = board.getValidMoves();

        if(board.isWhiteTurn){
            float value = -Float.MAX_VALUE;
            for(int[] move : moveList){
                ChessBoard potentialBoard = board.applyMoveUnsafe(move[0], move[1], move[2], move[3]);
                value = Math.max(value, alphaBetaSearch(potentialBoard, depth - 1, alpha, beta));
                alpha = Math.max(alpha, value);
                if(alpha >= beta) break;
            }
            return value;
        }else{
            float value = Float.MAX_VALUE;
            for(int[] move : moveList){
                ChessBoard potentialBoard = board.applyMoveUnsafe(move[0], move[1], move[2], move[3]);
                value = Math.min(value, alphaBetaSearch(potentialBoard, depth - 1, alpha, beta));
                beta = Math.min(beta, value);
                if(alpha >= beta) break;
            }
            return value;
        }
    }

    private float simpleAnalysis(ChessBoard board){

        if(board.isCheckMate()){
            return board.isWhiteTurn ? -1000.0f : 1000.0f;
        }

        float deltaMaterial = 0;

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                deltaMaterial += calculatePieceValue(board, x, y);
            }
        }

        float squareControlAdvantage = 0;

        for(int x1 = 0; x1 < 8; x1++){
            for(int y1 = 0; y1 < 8; y1++){
                for(int x2 = 0; x2 < 8; x2++){
                    for(int y2 = 0; y2 < 8; y2++){

                        if(board.isValidMove(x1, y1, x2, y2, false)){
                            squareControlAdvantage += (fileValue[x2] + rankValue[y2]) / (calculatePieceValue(board, x1, y1) + 1);
                        }
                    }
                }
            }
        }

        return deltaMaterial + squareControlAdvantage;
    }

    public float calculatePieceValue(ChessBoard board, int x, int y){

        switch(board.boardState[x][y]){
            case wPawn:
                return pawnValue[y];
            case bPawn:
                return -pawnValue[7-y];
            case wKing:
            case bKing:
                return 0.0f;
            default:
                return board.boardState[x][y].value;
        }
    }
}
