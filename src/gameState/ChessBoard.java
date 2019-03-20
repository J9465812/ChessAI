package gameState;

// Implementation of chess using 0x88 board representation;

import ai.AdvancedAI;

import java.text.Format;
import java.util.*;

public class ChessBoard implements java.lang.Cloneable {

    public static final byte NO_CHECK = -128;

    private static final int FLAG_IDX = 0;
    private static final int WHITE_KING_IDX = 1;
    private static final int BLACK_KING_IDX = 2;
    private static final int DIRECT_CHECK_IDX = 3;
    private static final int DISCOVERED_CHECK_IDX = 4;
    private static final int EN_PASSANT_IDX = 5;

    private static final short[] DEGREE_DIR;
    private static final byte [] ANGLE_DIFF;
    private static final short[] CHEBYSHEV_DIS;

    // format: angle modulus, angle1, distance, angle2, distance ...

    private static final short[][] VALID_PIECE_MOVES = {{/* EMPTY */}, {/* PAWN */ 360, 45, 1, 90, 2, 135, 1}, {/* KNIGHT */ 90, 27, 2, 63, 2}, {/* BISHOP */ 90, 45, 255}, {/* ROOK */ 90, 0, 255}, {/* QUEEN */ 45, 0, 255}, {/* KING */ 180, 0, 2, 45, 1, 90, 1, 135, 1}};

    static{

        DEGREE_DIR = new short[240];
        ANGLE_DIFF = new byte[360];
        CHEBYSHEV_DIS = new short[240];

        byte _00 = get0x88((byte)0, (byte)0);
        byte _07 = get0x88((byte)0, (byte)7);
        byte _70 = get0x88((byte)7, (byte)0);
        byte _77 = get0x88((byte)7, (byte)7);

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){

                byte square = get0x88((byte)x, (byte)y);

                short deg1 = degree(x, y, 0, 0);
                short deg2 = degree(x, y, 0, 7);
                short deg3 = degree(x, y, 7, 0);
                short deg4 = degree(x, y, 7, 7);

                if(isRelPrime(x, y))            ANGLE_DIFF[deg1] = (byte)(_00 - square);

                if(isRelPrime(x, 7 - y))        ANGLE_DIFF[deg2] = (byte)(_07 - square);

                if(isRelPrime(7 - x, y))        ANGLE_DIFF[deg3] = (byte)(_70 - square);

                if(isRelPrime(7 - x, 7 - y))    ANGLE_DIFF[deg4] = (byte)(_77 - square);

                DEGREE_DIR[Byte.toUnsignedInt(getDiff(square, _00))] = deg1;
                DEGREE_DIR[Byte.toUnsignedInt(getDiff(square, _07))] = deg2;
                DEGREE_DIR[Byte.toUnsignedInt(getDiff(square, _70))] = deg3;
                DEGREE_DIR[Byte.toUnsignedInt(getDiff(square, _77))] = deg4;

                CHEBYSHEV_DIS[Byte.toUnsignedInt(getDiff(square, _00))] = chebyshev(x, y, 0, 0);
                CHEBYSHEV_DIS[Byte.toUnsignedInt(getDiff(square, _07))] = chebyshev(x, y, 0, 7);
                CHEBYSHEV_DIS[Byte.toUnsignedInt(getDiff(square, _70))] = chebyshev(x, y, 7, 0);
                CHEBYSHEV_DIS[Byte.toUnsignedInt(getDiff(square, _77))] = chebyshev(x, y, 7, 7);
            }
        }
    }

    private static boolean isRelPrime(int a1, int a2){
        return a1 == 1 || a2 == 1 || ((a1%2 != 0 || a2%2 != 0) && (a1%3 != 0 || a2%3 != 0) && (a1%5 != 0 || a2%5 != 0) && (a1%7 != 0 || a2%7 != 0));
    }

    private static short degree(int x1, int y1, int x2, int y2){

        if(x1 == x2 && y1 == y2) return 0;

        double degreeAngle = Math.toDegrees(Math.atan2(y2-y1, x2-x1));

        short angle = (short)Math.round(degreeAngle);

        if(angle < 0) angle += 360;

        return angle;
    }

    private static short chebyshev(int x1, int y1, int x2, int y2){
        return (short)Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    /*
    0 = empty
    1 = pawn
    2 = knight
    3 = bishop
    4 = rook
    5 = queen
    6 = king
     */

    public byte[] pieces; // bits 1-4 = piece type    bit 5 = has moved   bit 6 = can e.p.
    public byte[] colors; // 0 = empty/white, 1 = black
    public short[] pins; // -1 = unpinned, >-1 = pinning direction
    public byte[] extraInfo;

    /* HELPER FUNCTIONS */
    public static boolean isSquare(byte square){return (square & 0x88) == 0;}
    public static byte getRank(byte square){return (byte)(square >> 4);}
    public static byte getFile(byte square){return (byte)(square & 7);}
    public static byte get0x88(byte file, byte rank){return (byte)((rank<<4)+file);}
    public static byte getDiff(byte sqr1, byte sqr2){return (byte)(0x77 + sqr1 - sqr2);}
    public static short getDis(byte sqr1, byte sqr2){return CHEBYSHEV_DIS[Byte.toUnsignedInt(getDiff(sqr1, sqr2))];}
    public static short getDir(byte sqr1, byte sqr2){return DEGREE_DIR[Byte.toUnsignedInt(getDiff(sqr1, sqr2))];}
    public static byte dir2Diff(short dir){return ANGLE_DIFF[dir];}
    public static short reverseAngle(short dir){return (short)((dir+180)%360);}
    public static boolean isBetween(byte e1, byte middle, byte e2){return e1 == middle || e2 == middle || getDir(e1, middle) == getDir(middle, e2);}

    public byte getActivePlayer(){return getFlag(0);}
    public void setActivePlayer(boolean isWhite){setFlag(0, !isWhite);}
    public byte getDirectCheck(){return extraInfo[DIRECT_CHECK_IDX];}
    public byte getDiscoveredCheck(){return extraInfo[DISCOVERED_CHECK_IDX];}


    private byte getFlag(int fId){return (byte)((extraInfo[FLAG_IDX] >> fId) & 1);}
    private void setFlag(int fId, boolean value){
        if(getFlag(fId) > 0) extraInfo[FLAG_IDX] -= 1 << fId;
        if(value) extraInfo[FLAG_IDX] += 1 << fId;
    }

    public ChessBoard(){
        this(true);
    }

    private ChessBoard(boolean initialState){

        if(!initialState) return;

        pieces = new byte[]{4, 2, 3, 5, 6, 3, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0,
                        1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                        4, 2, 3, 5, 6, 3, 2, 4};

        colors = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                        1, 1, 1, 1, 1, 1, 1, 1};

        pins = new short[120];

        extraInfo = new byte[128];
        extraInfo[FLAG_IDX] = 0;
        extraInfo[WHITE_KING_IDX] = 4;
        extraInfo[BLACK_KING_IDX] = 116;
        extraInfo[DIRECT_CHECK_IDX] = (byte)0x80;
        extraInfo[DISCOVERED_CHECK_IDX] = (byte)0x80;

        resetPinInfo();
    }

    public List<Integer> getValidMoves(byte color){

        List<Integer> moves = new ArrayList<>(100);

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){

                byte square = get0x88((byte)x, (byte)y);
                int usquare = Byte.toUnsignedInt(square);

                byte piece = (byte)(pieces[usquare] & 0x07);
                short pinDir = pins[usquare];

                if(pieces[usquare] == 0 || colors[usquare] != color) continue;

                for(int angle = 0; angle < 360; angle += VALID_PIECE_MOVES[piece][0]){
                    for(int n = 1; n < VALID_PIECE_MOVES[piece].length; n += 2){

                        short final_angle = (short)(angle + VALID_PIECE_MOVES[piece][n]);

                        if(pinDir > 0 && final_angle != pinDir && reverseAngle(final_angle) != pinDir) continue;

                        if(color == 1 && final_angle > 0) final_angle = (short)(360 - final_angle);

                        int diff = dir2Diff(final_angle);

                        for(byte dest = (byte)(square + diff);; dest += diff){
                            int udest = Byte.toUnsignedInt(dest);
                            if(!isSquare(dest) || getDis(square, dest) > VALID_PIECE_MOVES[piece][n+1] || (pieces[udest] > 0 && colors[udest] == color)) break;
                            if(isValidMove(square, dest)) moves.add((udest << 8) + usquare);
                            if(pieces[udest] > 0) break;
                        }
                    }
                }
            }
        }

        return moves;
    }

    public boolean isCorrectPlayer(int move){
        return colors[move & 0x000000ff] == getActivePlayer();
    }

    public boolean isValidMove(int move){
        return isValidMove((byte)(move & 0x000000ff), (byte)((move & 0x0000ff00) >> 8));
    }

    public boolean isValidMove(byte from, byte to){

        int ufrom = Byte.toUnsignedInt(from);
        int uto = Byte.toUnsignedInt(to);

        if(!isSquare(from) || !isSquare(to)) return false; // source and destination must be valid squares

        if(from == to) return false; // source and destination must be distinct

        if(pieces[ufrom] == 0) return false;

        if(pieces[uto] > 0 && colors[ufrom] == colors[uto]) return false; // a piece cannot capture another of the same color

        short dis = getDis(from, to);
        short dir = getDir(from, to);

        if(pins[ufrom] > -1 && !(dir == pins[ufrom] || reverseAngle(dir) == pins[ufrom])) return false; // a pinned piece must move along the direction of the pin

        if(colors[ufrom] > 0 && dir > 0) dir = (short)(360 - dir); // for black pawns

        int piece = pieces[ufrom] & 0x07;

        if(!pieceCanMove((byte)piece, dir, dis)) return false; // the direction and distance must be compatible with the moving piece

        byte diff = dir2Diff(getDir(from, to));

        for(byte i = (byte)(from + diff); i != to; i += diff){ // squares in the middle must be empty
            if(pieces[Byte.toUnsignedInt(i)] != 0) return false;
        }

        // MISCELLANEOUS CHECKS //

        // pawn mechanics //
        if(piece == 1){

            int rankDiff = Math.abs(getRank(to) - getRank(from));
            int fileDiff = Math.abs(getFile(to) - getFile(from));

            if(rankDiff == 2){
                if(pieces[uto] > 0 || (getRank(from) > 1 && getRank(from) < 6)){
                    return false;
                }
            }else if(fileDiff == 1){
                if(pieces[uto] == 0){
                    return false;
                }
            }else if(pieces[uto] > 0){
                return false;
            }
        }

        // castling mechanics //
        if(piece == 6){

            byte kingFile = getFile(from);
            byte castleRank = getRank(from);

            byte fileDiff = (byte)Math.abs(getFile(to) - kingFile);

            if(fileDiff == 2){

                if(pieces[ufrom] >= 8) return false;

                if(getDirectCheck() != NO_CHECK || getDiscoveredCheck() != NO_CHECK) return false;

                byte rookFile = (getFile(to) > kingFile) ? (byte)7 : 0;

                byte rookLocation = get0x88(rookFile, castleRank);
                int urookLocation = Byte.toUnsignedInt(rookLocation);

                if(pieces[urookLocation] != 4) return false;

                for(byte square = (byte)(Math.min(urookLocation, ufrom) + 1); square < Math.max(urookLocation, ufrom); square++){
                    if(pieces[square] != 0) return false;
                }

                if(playerSquareControl((byte)(Math.min(from, to) + 1), (byte)(1 - colors[ufrom])) > 0) return false;
            }
        }

        // END MISCELLANEOUS CHECKS //

        // CHECK MECHANICS //

        byte direct = getDirectCheck();
        byte discovered = getDiscoveredCheck();

        byte kingPos = (colors[ufrom] == 0) ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];

        if(piece == 6){

            if(playerSquareControl(to, (byte)(1 - colors[ufrom])) > 0) return false;

        }else{

            // make sure move blocks any checks

            if(direct != NO_CHECK || discovered != NO_CHECK){

                if(direct != NO_CHECK && discovered != NO_CHECK){
                    return false; // a single move can't block a double-check
                }

                if(direct != NO_CHECK && !isBetween(kingPos, to, direct)){
                    return false;
                }

                if(discovered != NO_CHECK && !isBetween(kingPos, to, discovered)){
                    return false;
                }
            }
        }

        return true;
    }

    // returns the number of pieces of the specified color attacking the specified square
    public int playerSquareControl(byte square, byte player){
        int controllers = 0;
        for(int pieceType = 1; pieceType < VALID_PIECE_MOVES.length; pieceType++){
            for(int n = 1; n < VALID_PIECE_MOVES[pieceType].length; n += 2){
                int maxDist = VALID_PIECE_MOVES[pieceType][n+1];
                for(int dir = VALID_PIECE_MOVES[pieceType][n]; dir < 360; dir += VALID_PIECE_MOVES[pieceType][0]){
                    byte diff = dir2Diff((short)dir);
                    if(player == 0) diff *= -1;
                    byte i = square;
                    while(true){
                        i += diff;
                        if(!isSquare(i)) break;
                        if(getDis(i, square) > maxDist) break;
                        int ui = Byte.toUnsignedInt(i);
                        if(pieces[ui] == 0 || (pieces[ui] & 0x07) == 6) continue;
                        if(colors[ui] != player) break;
                        if((pieces[ui] & 0x07) != pieceType) break;
                        if(pieceType == 1){
                            if(dir == 90 || dir == 270) break;
                        }
                        controllers ++;
                        break;
                    }
                }
            }
        }
        return controllers;
    }

    public void makeMove(int move){
        makeMove((byte)(move & 0x000000ff), (byte)((move & 0x0000ff00) >> 8));
    }

    public void makeMove(byte from, byte to){

        int ufrom = Byte.toUnsignedInt(from);
        int uto = Byte.toUnsignedInt(to);

        pieces[uto] = (byte)(pieces[ufrom] | 0x08);
        colors[uto] = colors[ufrom];
        pieces[ufrom] = 0;
        colors[ufrom] = 0;

        pins[ufrom] = -1;
        pins[uto] = -1;

        if((pieces[uto] & 0x07) == 6){

            if(colors[uto] == 0){
                extraInfo[WHITE_KING_IDX] = to;
            }else{
                extraInfo[BLACK_KING_IDX] = to;
            }

            byte kingFile = getFile(from);

            byte fileDiff = (byte)Math.abs(getFile(to) - kingFile);

            if(fileDiff == 2){

                byte castleRank = getRank(from);

                byte rookFile = (getFile(to) > kingFile) ? (byte)7 : 0;

                int urookFrom = Byte.toUnsignedInt(get0x88(rookFile, castleRank));
                int urookTo = Math.min(ufrom, uto) + 1;

                pieces[urookTo] = 12;
                colors[urookTo] = colors[urookFrom];
                pieces[urookFrom] = 0;
                colors[urookFrom] = 0;
            }

            resetPinInfo();

            resetCheck((byte)(1 - colors[uto]));

        }else {

            byte king1Pos = (colors[uto] == 0) ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];
            byte king2Pos = (colors[uto] != 0) ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];

            short dir1 = getDir(king1Pos, to);
            short dir2 = getDir(king1Pos, from);

            updatePinRay(king1Pos, dir1);
            if(dir1 != dir2) updatePinRay(king1Pos, dir2);

            dir1 = getDir(king2Pos, to);
            dir2 = getDir(king2Pos, from);

            extraInfo[DIRECT_CHECK_IDX] = updatePinRay(king2Pos, dir1);
            if(dir1 != dir2) extraInfo[DISCOVERED_CHECK_IDX] = updatePinRay(king2Pos, dir2);

        }

        setActivePlayer(getActivePlayer() != 0);
    }

    public int generateMoveMetadata(int move){

        move = move & 0x0000ffff; // erase any previous metadata

        move += (pieces[move >> 8]) << 16; // captured piece

        move += ((pieces[move & 0xff] & 0x08) << 17); // is not first move

        return move;
    }

    public void unmakeMove(int move){

        int ufrom = move & 0x000000ff;
        int uto = (move >> 8) & 0x000000ff;
        byte captured_piece = (byte)((move >> 16) & 0x0f);

        byte from = (byte)ufrom;
        byte to = (byte)uto;

        pieces[ufrom] = (byte)(pieces[uto] - 8 + ((move >> 17) & 0x08)); // restore hasMoved flag
        colors[ufrom] = colors[uto];

        pieces[uto] = captured_piece;
        if(captured_piece > 0){
            colors[uto] = (byte)(1 - colors[uto]);
        }else{
            colors[uto] = 0;
        }

        if((pieces[ufrom] & 0x07) == 6){

            if(colors[ufrom] == 0){
                extraInfo[WHITE_KING_IDX] = from;
            }else{
                extraInfo[BLACK_KING_IDX] = from;
            }

            byte fileDiff = (byte)Math.abs(getFile(to) - getFile(from));

            if(fileDiff == 2){

                byte castleRank = getRank(from);

                byte rookFile = (getFile(to) > getFile(from)) ? (byte)7 : 0;

                int urookTo = Byte.toUnsignedInt(get0x88(rookFile, castleRank));
                int urookFrom = Math.min(ufrom, uto) + 1;

                pieces[urookTo] = 4;
                colors[urookTo] = colors[urookFrom];
                pieces[urookFrom] = 0;
                colors[urookFrom] = 0;
            }

            resetPinInfo();

            resetCheck((byte)(1 - colors[ufrom]));

        }else{

            pins[ufrom] = -1;
            pins[uto] = -1;

            byte king1Pos = (colors[ufrom] == 0) ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];
            byte king2Pos = (colors[ufrom] != 0) ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];

            updatePinRay(king1Pos, getDir(king1Pos, from));
            extraInfo[DIRECT_CHECK_IDX] = updatePinRay(king1Pos, getDir(king1Pos, to));
            updatePinRay(king2Pos, getDir(king2Pos, from));
            updatePinRay(king2Pos, getDir(king2Pos, to));

        }

        setActivePlayer(getActivePlayer() != 0);
    }

    public void resetPinInfo(){

        for(int n = 0; n < 120; n++) pins[n] = -1;

        byte wKingPos = extraInfo[WHITE_KING_IDX];
        byte bKingPos = extraInfo[BLACK_KING_IDX];

        updatePinRay(wKingPos, (short)0);
        updatePinRay(wKingPos, (short)45);
        updatePinRay(wKingPos, (short)90);
        updatePinRay(wKingPos, (short)135);
        updatePinRay(wKingPos, (short)180);
        updatePinRay(wKingPos, (short)225);
        updatePinRay(wKingPos, (short)270);
        updatePinRay(wKingPos, (short)315);

        updatePinRay(bKingPos, (short)0);
        updatePinRay(bKingPos, (short)45);
        updatePinRay(bKingPos, (short)90);
        updatePinRay(bKingPos, (short)135);
        updatePinRay(bKingPos, (short)180);
        updatePinRay(bKingPos, (short)225);
        updatePinRay(bKingPos, (short)270);
        updatePinRay(bKingPos, (short)215);
    }

    public void resetCheck(byte player){

        extraInfo[DIRECT_CHECK_IDX] = NO_CHECK;
        extraInfo[DISCOVERED_CHECK_IDX] = NO_CHECK;

        byte kingPos = player == 0 ? extraInfo[WHITE_KING_IDX] : extraInfo[BLACK_KING_IDX];

        //TODO: Optimize resetCheck()

        for(byte n = 0; n < 120; n++){
            if(isValidMove(n, kingPos)){
                if(extraInfo[DIRECT_CHECK_IDX] != NO_CHECK){
                    extraInfo[DIRECT_CHECK_IDX] = n;
                }else{
                    extraInfo[DISCOVERED_CHECK_IDX] = n;
                }
            }
        }
    }

    // returns NO_CHECK if king is not in check, otherwise returns the square of the checking piece
    private byte updatePinRay(byte square, short dir){
        byte diff = dir2Diff(dir);
        int usquare = Byte.toUnsignedInt(square);
        if((pieces[usquare] & 0x07) != 6) return NO_CHECK;
        byte kingColor = colors[usquare];
        byte pinningSquare = -1;
        byte i = square;
        while(true){
            i += diff;
            if(!isSquare(i)){
                if(pinningSquare != -1)
                    pins[Byte.toUnsignedInt(pinningSquare)] = -1;
                return NO_CHECK;
            }
            int ui = Byte.toUnsignedInt(i);
            if(pieces[ui] == 0) continue;
            if(colors[ui] == kingColor){
                if(pinningSquare == -1){
                    pinningSquare = i;
                }else{
                    pins[Byte.toUnsignedInt(pinningSquare)] = -1;
                    pins[Byte.toUnsignedInt(i)] = -1;
                    return NO_CHECK;
                }
            }else{
                if(colors[ui] != kingColor){
                    if(pinningSquare == -1){
                        if(pieceCanMove(pieces[ui], reverseAngle(dir), getDis(square, i))) return i;
                        return NO_CHECK;
                    }else if(pieceCanMove(pieces[ui], reverseAngle(dir), getDis(square, i))){
                        pins[Byte.toUnsignedInt(pinningSquare)] = dir;
                        return NO_CHECK;
                    }
                }
            }
        }
    }


    // NOTE: not sufficient to detect valid moves
    private static boolean pieceCanMove(byte piece, short dir, short dis){

        if(dis == 0) return false;

        int pieceIdx = piece & 0x07;

        int mdir = dir % VALID_PIECE_MOVES[pieceIdx][0];
        for(int n = 1; n < VALID_PIECE_MOVES[pieceIdx].length; n+=2){
            if(mdir == VALID_PIECE_MOVES[pieceIdx][n] && dis <= VALID_PIECE_MOVES[pieceIdx][n+1]){
                return true;
            }
        }
        return false;
    }

    private static String generateSGRCode(int... args){

        String out = "";

        for(int arg : args){
            out += ";" + arg;
        }

        return (char)0x1b + "[" + out.substring(1) + "m";
    }

    private static final char[] pieceSymbols = {' ', '\u2659', '\u2658', '\u2657', '\u2656', '\u2655', '\u2654', '\u265F', '\u265E', '\u265D', '\u265C', '\u265B', '\u265A'};

    public Object clone(){

        ChessBoard board = new ChessBoard(false);

        board.pieces = this.pieces.clone();
        board.colors = this.colors.clone();
        board.pins = this.pins.clone();
        board.extraInfo = this.extraInfo.clone();

        return board;
    }

    public String toString(){
        return toString(false);
    }

    public String toString(boolean debug){

        StringBuilder sb = new StringBuilder("\t A\t B\t C\t D\t E\t F\t G\t H\n");

        for(int y = 7; y >= 0; y--){

            sb.append("" + (y + 1) + "\t");

            for(int x = 0; x < 8; x++){

                if(!debug) sb.append(generateSGRCode((x + y) % 2 == 0 ? 47 : 107));
                sb.append(" " + pieceSymbols[(pieces[get0x88((byte)x, (byte)y)] & 0x07) + 6 * colors[get0x88((byte)x, (byte)y)]] + "\t");
                if(!debug) sb.append(generateSGRCode(107));
            }
            if(!debug) sb.append(generateSGRCode(107));

            if(debug){

                sb.append("   ");

                for(int x = 0; x < 8; x++){
                    sb.append("{" + (pieces[get0x88((byte)x, (byte)y)]) + "," + pins[get0x88((byte)x, (byte)y)] + "," + colors[get0x88((byte)x, (byte)y)] + "} ");
                }
            }

            sb.append("\n");
        }

        if(debug){
            sb.append("\n");
            sb.append("Active Player: " + getActivePlayer() + ", Direct Check: " + getDirectCheck() + ", Discovered Check: " + getDiscoveredCheck());
        }

        return sb.toString();
    }

    public static void main(String[] args){

        /*ChessBoard initialState = new ChessBoard();

        System.out.println(initialState.toString());

        System.out.println(initialState.getValidMoves((byte)0).size() + "\n");

        long time = System.currentTimeMillis();

        int scans = 0;

        for(int t = 0; t < 1000000; t++) {
            initialState.getValidMoves((byte)0);
            scans++;
        }

        time = System.currentTimeMillis() - time;

        System.out.println("Checked " + scans + " scans in " + time + "ms");
        System.out.println("Move Checking Rate: " + scans*1000/time + " scans/sec");
        System.out.println("Move Checking Rate: " + ((double)(time)/scans*1000000) + " ns/scan");*/

        /*Scanner scan = new Scanner(System.in);

        ChessBoard initialState = new ChessBoard();

        Stack<Integer> moves = new Stack<>();

        while(true){

            System.out.println(initialState.toString(true));

            String moveS = scan.nextLine();

            if(moveS.equals("u")){

                initialState.unmakeMove(moves.pop());

            }else{

                int move = parseMove(moveS);

                if(!initialState.isValidMove(move) || !initialState.isCorrectPlayer(move)){
                    System.out.println("Invalid move.");
                    continue;
                }

                move = initialState.generateMoveMetadata(move);

                initialState.makeMove(move);

                moves.push(move);
            }
        }*/

        ChessBoard board = new ChessBoard();
        Scanner scanner = new Scanner(System.in);

        ai.ChessAI ai = new AdvancedAI();

        Stack<Integer> moves = new Stack<>();

        while(true){

            System.out.println(board.toString());

            System.out.println("It is your turn.");

            int move = 0;

            while(true) {

                System.out.println("Your move is: ");

                String line = scanner.nextLine();

                if(line.equals("ai")){
                    int aiMove = ai.getAIMove(board);

                    System.out.println("\n\nAI suggestion: " + moveToString(aiMove) + "\n");
                    continue;
                }

                if(line.equals("u")){
                    board.unmakeMove(moves.pop());
                    board.unmakeMove(moves.pop());
                    System.out.println(board.toString());
                    continue;
                }

                try {
                    move = parseMove(line);
                }catch (Exception e){}

                if(board.isValidMove(move)){
                    break;
                }

                System.out.print("\nThat move is invalid. ");
            }

            moves.push(board.generateMoveMetadata(move));

            board.makeMove(move);
            System.out.println(board.toString());

            int aiMove = ai.getAIMove(board);

            if(!board.isValidMove(aiMove)){
                System.out.println("AI move not valid.");
                System.exit(0);
            }

            moves.push(board.generateMoveMetadata(aiMove));

            board.makeMove(aiMove);
            System.out.println();
        }
    }

    private static final String RANKS = "abcdefgh";
    private static final String FILES = "12345678";

    public static int parseMove(String move){

        char[] chars = move.toCharArray();

        if(chars[2] != '-') return 0;

        return (RANKS.indexOf(chars[0])) | (FILES.indexOf(chars[1]) << 4) + (RANKS.indexOf(chars[3]) << 8) + (FILES.indexOf(chars[4]) << 12);
    }

    public static String moveToString(int move){
        return "" + RANKS.charAt(move & 0x0f) + FILES.charAt((move >> 4) & 0x0f) + "-" + RANKS.charAt((move >> 8) & 0x0f) + FILES.charAt((move >> 12) & 0x0f);
    }
}
