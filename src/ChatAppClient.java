import java.io.*;
import java.net.Socket;

/**
 * Created by Brigitte on 2017/04/02.
 */
public class ChatAppClient {
    public static void main(String args[]){
        String serverName = args[0];
        int portNumber = Integer.parseInt(args[1]);


        try {
            System.out.println("Connecting to server " + serverName + " on port " + portNumber);
            Socket client = new Socket(serverName,portNumber);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outputStream = client.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF("Hello from " + client.getLocalSocketAddress());
            InputStream inputStream = client.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            System.out.println("Server says " + dataInputStream.readUTF());

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
