package chessEngine.old.gameState;

import chessEngine.old.ai.AdvancedAI;
import chessEngine.old.ai.AlphaBetaAI;
import chessEngine.old.ai.ChessAI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChessBoard implements java.io.Serializable{

    private static final ChessPiece[][] _START_POSITION =
            {
                    {ChessPiece.wRook, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bRook},
                    {ChessPiece.wKnight, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bKnight},
                    {ChessPiece.wBish, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bBish},
                    {ChessPiece.wQueen, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bQueen},
                    {ChessPiece.wKing, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bKing},
                    {ChessPiece.wBish, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bBish},
                    {ChessPiece.wKnight, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bKnight},
                    {ChessPiece.wRook, ChessPiece.wPawn, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.none, ChessPiece.bPawn, ChessPiece.bRook},
            };

    public static final ChessBoard START_POSITION = new ChessBoard(_START_POSITION, new boolean[][]{{true, true}, {true, true}}, true);

    public final ChessPiece[][] boardState;
    public final boolean isWhiteTurn;
    public final boolean[][] castlingInfo;

    public ChessBoard(ChessPiece[][] boardState, boolean[][] castlingInfo, boolean isWhiteTurn){
        this.boardState = boardState;
        this.isWhiteTurn = isWhiteTurn;
        this.castlingInfo = castlingInfo;
    }

    public boolean isValidMove(ChessMove move){
        return isValidMove(move.sx, move.sy, move.ex, move.ey, true);
    }



    public boolean isValidMove(int sx, int sy, int ex, int ey, boolean checkColor){

        ChessPiece sourcePiece = boardState[sx][sy];
        ChessPiece endPiece = boardState[ex][ey];

        //System.out.println("S:" + sourcePiece.name());
        //System.out.println("E:" + endPiece.name());

        if(sourcePiece == ChessPiece.none || (sourcePiece.isWhite != isWhiteTurn && checkColor) || (!(endPiece == ChessPiece.none || endPiece == ChessPiece.wEpPawn || endPiece == ChessPiece.bEpPawn) && endPiece.isWhite == sourcePiece.isWhite)) return false;
        if(sx == ex && sy == ey) return false;

        //System.out.println("Move Passed Check 1");

        switch(sourcePiece){
            case wKing:
            case bKing:
                if(!isValidKingMove(sourcePiece.isWhite, sx, sy, ex, ey)) return false;
                break;
            case wRook:
            case bRook:
                if(!isValidRookMove(sx, sy, ex, ey)) return false;
                break;
            case wBish:
            case bBish:
                if(!isValidBishopMove(sx, sy, ex,ey)) return false;
                break;
            case wQueen:
            case bQueen:
                if(!isValidBishopMove(sx, sy, ex, ey) && !isValidRookMove(sx, sy, ex, ey)) return false;
                break;
            case wKnight:
            case bKnight:
                if(!isValidKnightMove(sx, sy, ex, ey)) return false;
                break;
            case wEpPawn:
            case bEpPawn:
                return false;
            case wPawn:
            case bPawn:
                if(!isValidPawnMove(sourcePiece.isWhite, endPiece, sx, sy, ex, ey)) return false;
                break;
        }

        //System.out.println("Move Passed Check 2");

        return !applyMoveUnsafe(sx, sy, ex, ey).isInCheck(sourcePiece.isWhite);
    }

    public int[][] getValidMoves(){

        List<int[]> validMoves = new ArrayList<>();

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(boardState[x][y] == ChessPiece.none) continue;
                for(int i = 0; i < 8; i++){
                    for(int j = 0; j < 8; j++){
                        if(isValidMove(x, y, i, j, true)) validMoves.add(new int[]{x, y, i, j});
                    }
                }
            }
        }

        return validMoves.toArray(new int[validMoves.size()][4]);
    }

    private boolean isValidKingMove(boolean isWhite, int sx, int sy, int ex, int ey){

        if(isWhite && sy == ey && sx == 4 && sy == 0){
            if(ex == 2 && castlingInfo[0][0]){
                return !isInCheck(true) && isValidMove(4, 0, 3, 0, false) && isValidMove(0, 0, 1, 0, false);
            }else if (ex == 6 && castlingInfo[0][1]){
                return !isInCheck(true) && isValidMove(4, 0, 5, 0, false);
            }
        }

        if(!isWhite && sy == ey && sx == 4 && sy == 7 && Math.abs(sx-ex) == 2){
            if(ex == 2 && castlingInfo[1][0]){
                return !isInCheck(false) && isValidMove(4, 7, 3, 7, false) && isValidMove(0, 7, 1, 7, false);
            }else if (ex == 6 && castlingInfo[1][1]){
                return !isInCheck(false) && isValidMove(4, 7, 5, 7, false);
            }
        }

        return Math.max(Math.abs(sx - ex), Math.abs(sy - ey)) == 1;
    }

    private boolean isValidRookMove(int sx, int sy, int ex, int ey){
        if(sx != ex && sy != ey) return false;
        if(sy == ey){
            for(int x = Math.min(sx, ex) + 1; x < Math.max(sx, ex); x++){
                if(boardState[x][sy] != ChessPiece.none && boardState[x][sy] != ChessPiece.wEpPawn && boardState[x][sy] != ChessPiece.bEpPawn) return false;
            }
        }else{
            for(int y = Math.min(sy, ey) + 1; y < Math.max(sy, ey); y++){
                if(boardState[sx][y] != ChessPiece.none && boardState[sx][y] != ChessPiece.wEpPawn && boardState[sx][y] != ChessPiece.bEpPawn) return false;
            }
        }
        return true;
    }

    private boolean isValidBishopMove(int sx, int sy, int ex, int ey){

        if(Math.abs(sx - ex) != Math.abs(sy - ey)) return false;
        int xd = sx < ex ? 1 : -1;
        int yd = sy < ey ? 1 : -1;
        for(int u = 1; u < Math.abs(sx - ex); u++){
            int x = sx + xd*u;
            int y = sy + yd*u;
            if(boardState[x][y] != ChessPiece.none && boardState[x][y] != ChessPiece.wEpPawn && boardState[x][y] != ChessPiece.bEpPawn) return false;
        }
        return true;
    }

    private boolean isValidKnightMove(int sx, int sy, int ex, int ey){
        return Math.abs(sx - ex) > 0 && Math.abs(sy - ey) > 0 && Math.abs(sx - ex) + Math.abs(sy - ey) == 3;
    }

    private boolean isValidPawnMove(boolean isWhite, ChessPiece endPiece, int sx, int sy, int ex, int ey){
        if(isWhite){
            if(sx == ex && ey == sy+1){
                return endPiece == ChessPiece.none;
            }else if(sx == ex && sy == 1 && ey == 3){
                return endPiece == ChessPiece.none && boardState[sx][2] == ChessPiece.none;
            }else if(Math.abs(sx - ex) == 1 && ey == sy+1){
                return endPiece != ChessPiece.none;
            }
            return false;
        }else{
            if(sx == ex && ey == sy-1){
                return endPiece == ChessPiece.none;
            }else if(sx == ex && sy == 6 && ey == 4){
                return endPiece == ChessPiece.none && boardState[sx][5] == ChessPiece.none;
            }else if(Math.abs(sx - ex) == 1 && ey == sy-1){
                return endPiece != ChessPiece.none;
            }
            return false;
        }
    }

    public ChessBoard applyMove(ChessMove move){

        if(!isValidMove(move)) return null;

        return applyMoveUnsafe(move.sx, move.sy, move.ex, move.ey);
    }

    public ChessBoard applyMoveUnsafe(int sx, int sy, int ex, int ey){

        ChessPiece[][] newBoard = new ChessPiece[8][8];

        boolean[][] newCastlingInfo = {{castlingInfo[0][0], castlingInfo[0][1]}, {castlingInfo[1][0], castlingInfo[1][1]}};

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                newBoard[x][y] = boardState[x][y];
            }
        }

        newBoard[ex][ey] = newBoard[sx][sy];
        newBoard[sx][sy] = ChessPiece.none;

        if(newBoard[ex][ey] == ChessPiece.wPawn && ey == 7){
            newBoard[ex][ey] = ChessPiece.wQueen;
        }

        /** Castling Info BEGIN **/

        if(newBoard[ex][ey] == ChessPiece.wKing){
            newCastlingInfo[0][0] = false;
            newCastlingInfo[0][1] = false;
        }

        if(newBoard[ex][ey] == ChessPiece.bKing){
            newCastlingInfo[1][0] = false;
            newCastlingInfo[1][1] = false;
        }

        if(newBoard[ex][ey] == ChessPiece.wRook){
            if(sx == 0){
                newCastlingInfo[0][0] = false;
            }else if(sx == 7){
                newCastlingInfo[0][1] = false;
            }
        }

        if(newBoard[ex][ey] == ChessPiece.bRook){
            if(sx == 0){
                newCastlingInfo[1][0] = false;
            }else if(sx == 7){
                newCastlingInfo[1][1] = false;
            }
        }

        if(!isWhiteTurn && ey == 0){
            if(ex == 0){
                newCastlingInfo[0][0] = false;
            }else if(ex == 7){
                newCastlingInfo[0][0] = false;
            }
        }

        if(isWhiteTurn && ey == 7){
            if(ex == 0){
                newCastlingInfo[1][0] = false;
            }else if(ex == 7){
                newCastlingInfo[1][0] = false;
            }
        }

        /** Castling Info END **/

        if(newBoard[ex][ey] == ChessPiece.wKing && sx == 4 && sy == 0){

            if(ex == 2){
                newBoard[0][0] = ChessPiece.none;
                newBoard[3][0] = ChessPiece.wRook;
            }else if(ex == 6){
                newBoard[7][0] = ChessPiece.none;
                newBoard[5][0] = ChessPiece.wRook;
            }
        }

        if(newBoard[ex][ey] == ChessPiece.bKing && sx == 4 && sy == 7){

            if(ex == 2){
                newBoard[0][7] = ChessPiece.none;
                newBoard[3][7] = ChessPiece.bRook;
            }else if(ex == 6){
                newBoard[7][7] = ChessPiece.none;
                newBoard[5][7] = ChessPiece.bRook;
            }
        }

        if(newBoard[ex][ey] == ChessPiece.bPawn && ey == 0){
            newBoard[ex][ey] = ChessPiece.bQueen;
        }

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(newBoard[x][y] == ChessPiece.wEpPawn || newBoard[x][y] == ChessPiece.bEpPawn){
                    newBoard[x][y] = ChessPiece.none;
                }
            }
        }

        if(Math.abs(sy - ey) == 2 && sx == ex){

            if(boardState[sx][sy] == ChessPiece.wPawn){
                newBoard[sx][sy + 1] = ChessPiece.wEpPawn;
            }else if(boardState[sx][sy] == ChessPiece.bPawn){
                newBoard[sx][sy - 1] = ChessPiece.bEpPawn;
            }
        }

        if((boardState[sx][sy] == ChessPiece.wPawn || boardState[sx][sy] == ChessPiece.bPawn)){

            if(boardState[ex][ey] == ChessPiece.wEpPawn){
                newBoard[ex][ey + 1] = ChessPiece.none;
            }else if(boardState[ex][ey] == ChessPiece.bEpPawn){
                newBoard[ex][ey - 1] = ChessPiece.none;
            }
        }

        return new ChessBoard(newBoard, newCastlingInfo, !isWhiteTurn);
    }

    public boolean isInCheck(boolean white){

        int kx = -1, ky = -1;

        for(int x = 0, y = 0; y < 8; x += x == 7 ? -7 : 1, y += x == 0 ? 1 : 0){
            if(boardState[x][y].isWhite == white && (boardState[x][y] == ChessPiece.wKing || boardState[x][y] == ChessPiece.bKing)){
                kx = x;
                ky = y;
                break;
            }
        }

        if(kx == -1 && ky == -1) return false; // Could not find king...

        for(int x = 0, y = 0; y < 8; x += x == 7 ? -7 : 1, y += x == 0 ? 1 : 0){

            if(boardState[x][y].isWhite == white || boardState[x][y] == ChessPiece.none) continue;

            switch(boardState[x][y]){
                case wKing:
                case bKing:
                    if(isValidKingMove(boardState[x][y].isWhite, x, y, kx, ky)) return true;
                    break;
                case wRook:
                case bRook:
                    if(isValidRookMove(x, y, kx, ky)) return true;
                    break;
                case wBish:
                case bBish:
                    if(isValidBishopMove(x, y, kx, ky)) return true;
                    break;
                case wQueen:
                case bQueen:
                    if(isValidBishopMove(x, y, kx, ky) || isValidRookMove(x, y, kx, ky)) return true;
                    break;
                case wKnight:
                case bKnight:
                    if(isValidKnightMove(x, y, kx, ky)) return true;
                    break;
                case wPawn:
                case bPawn:
                    if(isValidPawnMove(boardState[x][y].isWhite, boardState[kx][ky], x, y, kx, ky)) return true;
                    break;
            }
        }

        return false;
    }

    public int count(ChessPiece piece){

        int total = 0;

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(boardState[x][y] == piece) total ++;
            }
        }

        return total;
    }

    public boolean isCheckMate(){

        return isInCheck(isWhiteTurn) && getValidMoves().length == 0;
    }

    private static final String SGR_CODE_PRE = (char)0x1b + "[";
    private static final String SGR_CODE_AFT = "m";

    private static String generateSGRCode(int... args){

        String out = "";

        for(int arg : args){
            out += ";" + arg;
        }

        return SGR_CODE_PRE + out.substring(1) + SGR_CODE_AFT;
    }

    public String toString(){

        StringBuilder sb = new StringBuilder("\t A\t B\t C\t D\t E\t F\t G\t H\n");

        for(int y = 7; y >= 0; y--){

            sb.append("" + (y + 1) + "\t");

            for(int x = 0; x < 8; x++){

                sb.append(generateSGRCode((x + y) % 2 == 0 ? 47 : 107));
                sb.append(formatPiece(boardState[x][y]));
                sb.append(generateSGRCode(107));
            }
            //sb.append(generateSGRCode(100));
            //sb.append("\t");
            sb.append(generateSGRCode(107));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatPiece(ChessPiece piece){
        return " " + piece.symbol + "\t";
    }

    public static void main(String[] args){

        // AI

        ChessBoard board = START_POSITION;
        Scanner scanner = new Scanner(System.in);

        ChessAI ai = new AlphaBetaAI(4);

        while(true){

            System.out.println(board.toString());

            System.out.println("It is your turn.");

            ChessMove move = null;

            while(true) {

                System.out.println("Your move is: ");

                try {
                    move = ChessMove.parse(scanner.nextLine());
                }catch (Exception e){}

                if(move != null && board.isValidMove(move)){
                    break;
                }

                System.out.print("\nThat move is invalid. ");
            }

            board = board.applyMove(move);
            System.out.println(board.toString());

            if(board.isCheckMate()){
                System.out.println("White Wins.");
                System.exit(0);
            }

            ChessMove aiMove = ai.getAIMove(board);

            System.out.println("AI response: " + aiMove.toString() + "\n");

            board = board.applyMove(aiMove);
            System.out.println();

            if(board.isCheckMate()){
                System.out.println("Black Wins.");
                System.exit(0);
            }
        }

        // 2P

        /*ChessBoard board = START_POSITION;
        Scanner scanner = new Scanner(System.in);

        while(true){

            System.out.println(board.toString());

            System.out.println("It is " + (board.isWhiteTurn ? "white's" : "black's") + " turn.");

            ChessMove move;

            while(true) {
                System.out.println("The next move is: ");
                move = ChessMove.parse(scanner.nextLine());

                if(board.isValidMove(move)){
                    break;
                }

                System.out.print("\nThat move is invalid. ");
            }

            board = board.applyMoveUnsafe(move);
            System.out.println();
        }*/

        //2 AI

        /*ChessBoard board = START_POSITION;

        ChessAI ai = new AlphaBetaAI(2);

        System.out.println(board.toString());

        while(true){

            ChessMove wMove = ai.getAIMove(board);

            System.out.println("White's move: " + wMove.toString() + "\n");

            board = board.applyMove(wMove);
            System.out.println(board.toString());

            if(board.isCheckMate()){
                System.out.println("White Wins.");
                System.exit(0);
            }

            ChessMove bMove = ai.getAIMove(board);

            System.out.println("Black's move: " + bMove.toString() + "\n");

            board = board.applyMove(bMove);
            System.out.println(board.toString());

            if(board.isCheckMate()){
                System.out.println("Black Wins.");
                System.exit(0);
            }
        }*/

        // Speed Test

        /*System.out.println(START_POSITION.toString());

        long time = System.currentTimeMillis();

        int scans = 0;

        for(int t = 0; t < 1000000; t++) {
            START_POSITION.getValidMoves();
            scans++;
        }

        time = System.currentTimeMillis() - time;

        System.out.println("Checked " + scans + " scans in " + time + "ms");
        System.out.println("Move Checking Rate: " + scans*1000/time + " scans/sec");
        System.out.println("Move Checking Rate: " + ((double)(time)/scans*1000000) + " ns/scan");*/
    }
}
