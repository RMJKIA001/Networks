import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Brigitte on 2017/04/02.
 */
public class ChatAppClient {

    // I/O stuff
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private String server, username;
    private int port;


    // Constructor for client
    public ChatAppClient(String server, int port, String username){
        this.server = server;
        this.port = port;
        this.username = username;
    }

    // Starts the client and dialog with server
    public boolean start(){

        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            display("Error connecting to server: ");
            e.printStackTrace();
            return false;
        }
        String msg = "Connection accepted " + socket.getInetAddress() + ": " + socket.getPort();
        display(msg);

        // Create data streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream((socket.getOutputStream()));
        } catch (IOException e) {
            display("Exception in creating new streams");
            e.printStackTrace();
            return false;
        }

        // Create thread to listen from server
        new ServerListener().start();

        // Send username to server as String
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            display("Exception doing login");
            e.printStackTrace();
            disconnect();
            return false;
        }

        // Success that client connected to server
        return true;
    }

    public void display(String msg){
        System.out.println(msg);
    }
    public void disconnect(){
        try {
            sInput.close();
            sOutput.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(Message msg){
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Exception in sending message to the server");
            e.printStackTrace();
        }
    }
    public static void main (String[] args){
        //default values
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "HeWhoMustNotBeNamed";

        // get values from command line
        switch(args.length){
            case 3:
                serverAddress = args[2];
            case 2:
                portNumber = Integer.parseInt(args[1]);
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Use command: java Client [username] [portNumber] " +
                        "[serverAddress]");
                return;
        }
        // create the Client object
        ChatAppClient client = new ChatAppClient(serverAddress,portNumber,userName);

        // try to connect to server
        if (!client.start()){
            return;
        }

        // get messages from the client
        Scanner scanner = new Scanner(System.in);

        //infinite loop getting messages
        while (true){
            System.out.println("> ");
            String msg = scanner.nextLine();
            // sends message to server that client wants to logout
            if (msg.equalsIgnoreCase("LOGOUT")){
                client.sendMessage(new Message(Message.LOGOUT, ""));
                break;
            } else if (msg.equalsIgnoreCase("PICTURE")){
                // code here
            } else if (msg.equalsIgnoreCase(("WHOISIN"))){
                client.sendMessage(new Message(Message.WHOISIN, ""));
            } else {
                client.sendMessage(new Message(Message.MESSAGE, msg));
            }
        }
        client.disconnect();
    }



    class ServerListener extends Thread{

        public void run(){
            while (true){

                try {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                    System.out.println("> ");
                } catch (IOException e) {
                    display("Connection to server is closed");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
// FIRST ATTEMPT:
/*
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
*/