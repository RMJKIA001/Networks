import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Brigitte on 2017/04/03.
 */
public class ClientThread extends Thread {

    Socket socket;
    ObjectInputStream sInput;
    ObjectOutputStream sOutput;
    int id;
    String username;
    Message msg;
    String date;

    public ClientThread(Socket socket){

    }

    public boolean writeMsg(String msg){
        return false;
    }
}
