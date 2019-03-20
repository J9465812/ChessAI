package chessEngine.old.net;

import chessEngine.old.ai.AdvancedAI;
import chessEngine.old.ai.ChessAI;
import chessEngine.old.gameState.ChessBoard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;

public class ChessClient {

    private static final String PROMPT = ">>> : ";
    private static final String REMOVE_PROMPT = "\u0008\u0008\u0008\u0008\u0008\u0008";

    private ChessAI ai;

    private Socket socket;
    private String name;

    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public ChessClient(ChessAI ai, String name, String ip) throws IOException{

            this.ai = ai;
            socket = new Socket(ip, ChessProtocol.PORT_ID);

            if (!socket.isConnected()) {
                throw new IOException("No connection made.");
            }

            this.writer = new ObjectOutputStream(socket.getOutputStream());
            this.reader = new ObjectInputStream(socket.getInputStream());

            this.name = name;

            sendMessage(ChessProtocol.MSG_SERVER, ChessMessage.MessageType.SET_NAME);
    }

    public void sendMessage(String destination, ChessMessage.MessageType type, Object... args) throws IOException{
        writer.writeObject(new ChessMessage(this.name, destination, type, args));
        writer.flush();
    }

    public ChessMessage readMessage() throws IOException, ClassNotFoundException{

        return (ChessMessage) reader.readObject();
    }

    public void start(){

        System.out.print(" >>> : ");

        //new Thread(this::scanInput).start();

        try {

            while (true) {

                ChessMessage cm = null;

                try {
                    cm = readMessage();
                } catch (Exception e) {
                }

                if (cm != null) {

                    processMessage(cm);
                }
            }

        }finally {
            disconnect();
        }
    }

    private void scanInput(){

        Scanner scanner = new Scanner(System.in);

        while(true){

            String command = scanner.nextLine();

            String[] args = command.split("\\|");

            switch(args[0]){
                case "NewGame":
                    startGame(args[1]);
                    break;
            }
        }
    }

    private void processMessage(ChessMessage cm){

        switch(cm.type){
            case MAKE_MOVE:

                ChessBoard board = (ChessBoard) cm.args[0];

                print(Instant.now() + ": Move made for \"" + cm.source + "\" : \n");
                print(board.toString() + "\n\n");
                board.applyMove(ai.getAIMove(board));
                print(board.toString() + "\n");

                try {
                    sendMessage(cm.source, ChessMessage.MessageType.MAKE_MOVE, board);
                }catch(Exception e){
                    System.out.println("Exception thrown while returning message:");
                    e.printStackTrace();
                }

                break;
            case INVALID_NAME:
                print(Instant.now() + ": Received message from \"" + cm.source + "\" : Given name already used. Name set to \"" + cm.args[0].toString() + "\"\n");
                this.name = cm.args[0].toString();
                break;
            case CONNECTION_TEST:
                break;
            default:
                System.out.println(Instant.now() + ": Received message from \"" + cm.source + "\" : Invalid message type\n");
        }

        flushPrint();
    }

    public void disconnect(){
        try {
            socket.close();
            writer.close();
            reader.close();
        }catch(Exception e){
            System.exit(0);
        }
    }

    public void startGame(String otherPlayer){

        ChessBoard board = ChessBoard.START_POSITION;

        print(Instant.now() + ": Starting new game with \"" + otherPlayer + "\"\n");

        if(Math.random() > 0.5f){
            print("You are playing white.\n");
            board.applyMove(ai.getAIMove(board));
        }else{
            print("You are playing black.\n");
        }

        try {
            sendMessage(otherPlayer, ChessMessage.MessageType.MAKE_MOVE, board);
        }catch(Exception e){
            System.out.println("Exception thrown while starting game:");
            e.printStackTrace();
        }

        flushPrint();
    }

    public static void main(String[] args) throws IOException{

        new ChessClient(new AdvancedAI(4),"Client Test", "localhost").start();
    }

    private static StringBuilder outBuilder = new StringBuilder();

    public static void print(String s){
        outBuilder.append(s);
    }

    public static void flushPrint(){

        System.out.print(REMOVE_PROMPT + outBuilder.toString() + "\n" + PROMPT);

        outBuilder = new StringBuilder();
    }
}
