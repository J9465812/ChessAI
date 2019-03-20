package chessEngine.old.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ChessServer {

    private ServerSocket serverSocket;
    private List<ChessClientHandler> clients;

    public void start(){

        try{
            serverSocket = new ServerSocket(ChessProtocol.PORT_ID);
        }catch(Exception e){
            System.out.println("Failed to start server:");
            e.printStackTrace();
            return;
        }

        clients = new ArrayList<>();

        new Thread(this::checkConnections).start();

        try{
            System.out.println("Server Initialized... IP: " + InetAddress.getLocalHost().getHostAddress());
        }catch(Exception e){
            System.out.println("Server Initialized... IP: UNKNOWN");
        }

        while(true){

            Socket socket;

            try{
                socket = serverSocket.accept();

                System.out.println(Instant.now() + ": New Client Joining...");

                if(socket != null){

                    ChessClientHandler cch = new ChessClientHandler(socket);

                    clients.add(cch);

                    new Thread(cch::handle).start();

                    System.out.println(Instant.now() + ": New Client Joined. " + clients.size() + " clients total");
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void processMessage(ChessMessage cm, ChessClientHandler source){

        switch(cm.type){
            case DISPLAY:
                System.out.println(Instant.now() + ": Message from \"" + source.name + "\" : " + cm.args[0]);
                break;
            case SET_NAME:

                String name = generateAvailableName(cm.source);

                if(!name.equals(cm.source)){
                    try{
                        source.sendMessage(ChessMessage.MessageType.INVALID_NAME, name);
                    }catch(IOException e){
                        source.invalidate();
                    }
                }

                System.out.println(Instant.now() + ": Client \"" + source.name + "\" renamed to \"" + name + "\"");
                source.name = name;
                break;
        }
    }

    private String generateAvailableName(String base){

        if(isAvailable(base)) return base;

        for(int x = 2; ; x++){

            if(isAvailable(base + " " + x)){
                return base + " " + x;
            }
        }
    }

    private boolean isAvailable(String name){

        for(int n = 0; n < clients.size(); n++){

            ChessClientHandler cch = clients.get(n);

            if(cch.name.equals(name)){
                return false;
            }
        }

        return true;
    }

    private void checkConnections(){

        while(true){

            for(int n = 0; n < clients.size(); n++) {

                ChessClientHandler cch = clients.get(n);

                try{

                    cch.sendMessage(ChessMessage.MessageType.CONNECTION_TEST);

                }catch(Exception e){

                    System.out.println(Instant.now() + ": Client \"" + cch.name + "\" disconnected");
                    cch.invalidate();
                }
            }

            try{
                Thread.sleep(100);
            }catch(Exception e){}
        }
    }

    private class ChessClientHandler {

        private String name = ChessProtocol.MSG_UNNAMED;
        private Socket socket;

        private ObjectInputStream reader;
        private ObjectOutputStream writer;

        String getName() {
            return name;
        }

        ChessClientHandler(Socket socket) throws IOException{

            this.socket = socket;

            this.reader = new ObjectInputStream(socket.getInputStream());
            this.writer = new ObjectOutputStream(socket.getOutputStream());
        }

        void handle(){

            while(true){

                ChessMessage cm = null;

                try{
                    cm = readMessage();
                }catch(Exception e){}

                if(cm != null){

                    if(cm.destination.equals(ChessProtocol.MSG_SERVER)){
                        processMessage(cm, this);
                        continue;
                    }

                    System.out.println(Instant.now() + ": Msg from \"" + getName() + "\" to \"" + cm.destination + "\"");

                    for(int n = 0; n < clients.size(); n++){

                        ChessClientHandler destination = clients.get(n);

                        if(cm.destination.equals(ChessProtocol.MSG_ALL) || cm.destination.equals(destination.name)){
                            try{
                                destination.sendMessage(cm);
                            }catch(Exception e){
                                System.out.println("Error while transferring:");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        void sendMessage(ChessMessage msg) throws IOException{
            writer.writeObject(msg);
            writer.flush();
        }

        void sendMessage(ChessMessage.MessageType type, Object... args) throws IOException{
            writer.writeObject(new ChessMessage(ChessProtocol.MSG_SERVER, this.name, type, args));
            writer.flush();
        }

        ChessMessage readMessage() throws IOException, ClassNotFoundException{

            return (ChessMessage) reader.readObject();
        }


        // This method should be called after any exception caused by sending/receiving messages from this client.
        void invalidate(){
            try{
                socket.close();
                clients.remove(this);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        new ChessServer().start();
    }
}
