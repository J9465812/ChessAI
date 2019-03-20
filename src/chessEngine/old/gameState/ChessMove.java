package chessEngine.old.gameState;

import java.util.regex.Pattern;

public class ChessMove {

    public final int sx;
    public final int sy;
    public final int ex;
    public final int ey;

    public ChessMove(int sx, int sy, int ex, int ey) {
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
    }

    private static final String fileNames = "abcdefgh";
    private static final String rankNames = "12345678";
    private static final String regex = "[" + fileNames + "][" + rankNames + "]-[" + fileNames + "][" + rankNames + "]";

    public static ChessMove parse(String str){

        //System.out.println("\n\n\"" + str + "\"\n\n");

        Pattern p = Pattern.compile(regex);

        if(!p.matcher(str).matches()){
            throw new IllegalArgumentException("Could not parse to ChessMove: " + str);
        }

        int sx = fileNames.indexOf((int)str.charAt(0));
        int sy = rankNames.indexOf((int)str.charAt(1));
        int ex = fileNames.indexOf((int)str.charAt(3));
        int ey = rankNames.indexOf((int)str.charAt(4));

        return new ChessMove(sx, sy, ex, ey);
    }

    public String toString(){
        return fileNames.charAt(sx) + (rankNames.charAt(sy) + "-" + fileNames.charAt(ex)) + rankNames.charAt(ey);
    }

    public static void main(String[] args){

        ChessMove c = parse("b2-g7");
    }
}
