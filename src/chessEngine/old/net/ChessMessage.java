package chessEngine.old.net;

public class ChessMessage implements java.io.Serializable{

    public final String source;
    public final String destination;
    public final MessageType type;
    public final Object[] args;

    public enum MessageType {

        CONNECTION_TEST,
        SET_NAME,
        INVALID_NAME,
        DISPLAY,
        MAKE_MOVE,
    }

    public ChessMessage(String source, String destination, MessageType type, Object... args){
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.args = args;
    }
}
