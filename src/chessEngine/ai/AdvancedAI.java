package chessEngine.ai;

import chessEngine.gameState.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AdvancedAI implements ChessAI {

    public int depth = 3;

    private int analysedPositions;
    private int skippedPositions;

    public AdvancedAI(int depth){
        this.depth = depth;
    }

    @Override
    public ChessMove getAIMove(ChessBoard board) {

        analysedPositions = 0;
        skippedPositions = 0;

        ChessMove[] moveList = board.getValidMoves();

        List<ChessMove> moves = new ArrayList<>(Arrays.asList(moveList));

        Random r = new Random();

        float finalAnalysis = -10000.0f;
        ChessMove bestMove = null;

        System.out.print("Analysis: " + moveList.length + " moves: ");

        while(moves.size() > 0){

            ChessMove move = moves.remove(r.nextInt(moves.size()));

            ChessBoard potentialBoard = board.applyMove(move);

            float analysis = -deepAnalysis(potentialBoard, depth - 1);

            if(analysis >= finalAnalysis){
                finalAnalysis = analysis;
                bestMove = move;
            }

            System.out.printf(move.toString() + " -> %+.2f  ", analysis);
        }

        System.out.println("\nBest move : " + (bestMove == null ? "None" : bestMove.toString()) + " -> " + finalAnalysis);
        System.out.println(analysedPositions + " positions analyzed. " + skippedPositions + " skipped");

        return bestMove;
    }

    private float deepAnalysis(ChessBoard board, int depth){

        float initialAnalysis = simpleAnalysis(board);

        if(depth == 0){
            return initialAnalysis;
        }

        ChessMove[] moveList = board.getValidMoves();

        float finalAnalysis = -20000.0f;

        for(ChessMove move : moveList){

            ChessBoard potentialBoard = board.applyMove(move);

            float simpleAnalysis = -simpleAnalysis(potentialBoard);

            if(simpleAnalysis - simpleAnalysis > depth - this.depth - finalAnalysis + 1.5f){
                skippedPositions ++;
                continue;
            }

            float analysis = -deepAnalysis(potentialBoard, depth - 1);

            if(analysis > finalAnalysis) finalAnalysis = analysis;
        }

        return finalAnalysis;
    }

    private float simpleAnalysis(ChessBoard board){

        analysedPositions++;

        if(board.isCheckMate()){
            return -10000.0f;
        }

        int deltaMaterial = 0;

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(board.boardState[x][y].isWhite == board.isWhiteTurn && !(board.boardState[x][y] == ChessPiece.wKing || board.boardState[x][y] == ChessPiece.bKing)){
                    deltaMaterial += board.boardState[x][y].value;
                }else{
                    deltaMaterial -= board.boardState[x][y].value;
                }
            }
        }

        float squareControlAdvantage = 0;

        for(int x1 = 0; x1 < 8; x1++){
            for(int y1 = 0; y1 < 8; y1++){
                for(int x2 = 0; x2 < 8; x2++){
                    for(int y2 = 0; y2 < 8; y2++){

                        ChessMove move = new ChessMove(x1, y1, x2, y2);

                        if(board.isValidMove(move, false)){
                            if(board.isWhiteTurn == board.boardState[x1][y1].isWhite) {
                                squareControlAdvantage += 0.1f / (board.boardState[x1][y1].value * board.boardState[x1][y1].value);
                            }else{
                                squareControlAdvantage -= 0.1f / (board.boardState[x1][y1].value*board.boardState[x1][y1].value);
                            }
                        }
                    }
                }
            }
        }

        return deltaMaterial + squareControlAdvantage;
    }
}
