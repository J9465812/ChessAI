package chessEngine.net;

import chessEngine.ai.ChessAI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ChessServer {

    public static final int CHESS_PORT = 38519;

    private HashMap<String, Object> params;

    private ServerSocket serverSocket;
    private List<Socket> incommingConnections;
    private List<Socket> outgoingConnections;

    private String getString(String pname){
        return (String) params.get(pname);
    }

    private boolean getBoolean(String pname){
        return (Boolean) params.get(pname);
    }

    private int getInt(String pname){
        return (Integer) params.get(pname);
    }

    public ChessServer(ChessAI ai){

        params.put("ai_name", "AI-" + (int)(Math.random() * Integer.MAX_VALUE));
        params.put("ai_acceptAll", true);
        params.put("ai_engine", ai);

        try{

            serverSocket = new ServerSocket(CHESS_PORT);
            new Thread(() -> scanConnections()).start();

        }catch(Exception e){
            System.out.println("Error during server initialization:");
            e.printStackTrace();
        }
    }

    public void start(){

    }

    public void scanConnections(){

        try{

            /*while(true){

                serverSocket.accept();

                if()
            }*/

        }catch(Exception e){
            System.out.println("Error during scanning: (Scan ended)");
            e.printStackTrace();
        }
    }

    public static void main(String[] args){


    }
}
