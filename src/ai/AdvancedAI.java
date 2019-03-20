package ai;

import gameState.ChessBoard;

import java.util.*;

public class AdvancedAI implements ChessAI{

    public static final int[] PIECE_VALUE = {0, 10, 30, 30, 50, 90, 0};

    public static final int REALLY_BIG_NUMBER = 1000000;



    public int getAIMove(ChessBoard board) {
        return multiThreadedSearch(board, 5, 8);//Runtime.getRuntime().availableProcessors());
    }

    public synchronized int multiThreadedSearch(ChessBoard board, int depth, int numThreads){
        List<Integer> moveList = board.getValidMoves(board.getActivePlayer());

        HashMap<Integer, Integer> moves = new HashMap<>();
        HashMap<Integer, String> continuations = new HashMap<>();

        for(int move : moveList) moves.put(move, REALLY_BIG_NUMBER + 1);

        Thread[] threads = new Thread[numThreads];

        for(int n = 0; n < numThreads; n++){
            threads[n] = new Thread(() -> threadExec((ChessBoard)board.clone(), moves, continuations, depth));
            threads[n].start();
        }

        for(int n = 0; n < numThreads; n++){
            try{
                threads[n].join();
            }catch(InterruptedException e){
                n--;
            }
        }

        int bestScore = -REALLY_BIG_NUMBER;
        int bestMove = 0;

        for(int move : moveList){
            if(moves.get(move) >= bestScore){
                bestScore = moves.get(move);
                bestMove = move;
            }
        }

        System.out.println("AI: " + ChessBoard.moveToString(bestMove) + " : " + continuations.get(bestMove) + "-> " + bestScore);

        return bestMove;
    }

    private void threadExec(ChessBoard board, HashMap<Integer, Integer> moves, HashMap<Integer, String> continuations, int depth){

        while(moves.containsValue(REALLY_BIG_NUMBER + 1)){

            int move = 0;

            synchronized (moves){

                for(int possibleMove : moves.keySet()){
                    if(moves.get(possibleMove) == REALLY_BIG_NUMBER + 1){
                        move = possibleMove;
                    }
                }

                moves.put(move, -REALLY_BIG_NUMBER);
            }

            int metaMove = board.generateMoveMetadata(move);

            LinkedList<String> continuation = new LinkedList<>();

            board.makeMove(move);

            int score = -negaMax(board, -REALLY_BIG_NUMBER, REALLY_BIG_NUMBER, depth-1, continuation);

            board.unmakeMove(metaMove);

            moves.put(move, score);

            StringBuilder continuationStr = new StringBuilder();

            for(String m : continuation){
                continuationStr.insert(0, m + " ");
            }

            continuations.put(move, continuationStr.toString());

            continuationStr.insert(0, ChessBoard.moveToString(move) + " ");

            System.out.println("  " + continuationStr.toString() + "->" + score);
        }
    }

    private int negaMax(ChessBoard board, int alpha, int beta, int depth, List<String> continuation){

        if(depth == 0) return quiesce(board, alpha, beta);

        List<Integer> moves = board.getValidMoves(board.getActivePlayer());

        moves.sort((a, b) -> -compareMove(board, a, b)); // MVV-LVA heuristic

        for(int n = 0; n < depth; n++){
            continuation.add(0, "-");
        }

        for(int move : moves){

            int metaMove = board.generateMoveMetadata(move);

            continuation.add(0, ChessBoard.moveToString(move));

            board.makeMove(move);

            int score = -negaMax(board, -beta, -alpha, depth-1, continuation);

            board.unmakeMove(metaMove);

            if(score >= beta){
                for(int n = 0; n < depth; n++){
                    continuation.remove(depth);
                }
                return beta;
            }
            if(score > alpha){
                for(int n = 0; n < depth; n++){
                    continuation.remove(depth);
                }
                alpha = score;
            }else{
                for(int n = 0; n < depth; n++){
                    continuation.remove(0);
                }
            }
        }

        return alpha;
    }

    private int quiesce(ChessBoard board, int alpha, int beta){

        int standPat = evaluate(board);

        if(standPat >= beta) return beta;

        // DELTA PRUNING //

        if(standPat < alpha - 20) return alpha;

        if(standPat > alpha) alpha = standPat;

        List<Integer> allMoves = board.getValidMoves(board.getActivePlayer());

        for(int move : allMoves){
            if(board.pieces[(move & 0x0000ff00) >> 8] == 0) continue;

            int metaMove = board.generateMoveMetadata(move);
            board.makeMove(move);

            int score = -quiesce(board, -beta, -alpha);

            board.unmakeMove(metaMove);

            if( score >= beta ) return beta;
            if( score > alpha ) alpha = score;
        }

        return alpha;
    }

    private int evaluate(ChessBoard board){

        int who2move = 1-2*board.getActivePlayer();

        int advantage = 0;

        for(byte n = 0; n < 120; n++){

            if(ChessBoard.isSquare(n)){
                int un = Byte.toUnsignedInt(n);
                advantage += PIECE_VALUE[board.pieces[n]&0x07] * (1-2*board.colors[n]);
            }
        }

        List<Integer> whiteMoves = board.getValidMoves((byte)0);
        List<Integer> blackMoves = board.getValidMoves((byte)1);

        int whiteMobility = whiteMoves.size();
        int blackMobility = blackMoves.size();

        for(int n = 0; n < whiteMoves.size(); n++){
            if((board.pieces[whiteMoves.get(n) & 0x000000ff] & 0x07) >= 5) whiteMobility --;
        }

        for(int n = 0; n < blackMoves.size(); n++){
            if((board.pieces[blackMoves.get(n) & 0x000000ff] & 0x07) >= 5) blackMobility --;
        }

        advantage += whiteMobility - blackMobility;

        return advantage * who2move;
    }

    // compares moves based on MVV - LVA

    private int compareMove(ChessBoard board, int move1, int move2){

        int v1 = board.pieces[(move1 & 0x0000ff00) >> 8] & 0x07;
        int v2 = board.pieces[(move2 & 0x0000ff00) >> 8] & 0x07;

        int a1 = board.pieces[move1 & 0x000000ff] & 0x07;
        int a2 = board.pieces[move2 & 0x000000ff] & 0x07;

        if(v1 == v2) {
            if(a1 == a2) return 0;
            return a1 > a2 ? 1 : -1;
        }else{
            return v1 > v2 ? 1 : -1;
        }
    }
}
