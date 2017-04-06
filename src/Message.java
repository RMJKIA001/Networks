import java.io.Serializable;

/**
 * Created by Brigitte on 2017/04/03.
 */

/**
 * This class in a message object that will be sent to and from the server. This will make it easier logistically
 * for the client as they can query things from the server.
 * We can also had the picture functionality here.
 * The implements Serializable was recommended by Google
 */
public class Message implements Serializable{

    protected static final long serialVersionUID = 1112122200L;

    static final int WHOISIN = 0, MESSAGE = 1, PICTURE = 2, LOGOUT = 3, KICK = 4;

    private int type;
    private String message;

    Message(int type, String message){
        this.type = type;
        this.message = message;
    }

    public int getType(){
        return type;
    }
    public String getMessage(){
        return message;
    }
    //TODO: add getter for pictures
}
