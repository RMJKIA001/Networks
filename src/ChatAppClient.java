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
    private ServerListener sl;
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
        msg = " - Use command 'WHOISIN' to find what other users are connected. \n - Use command 'PICTURE' to send a picture. \n - Use command 'KICK [username]' to kick username off. \n - Use command 'DM:[username] [message]' to send a private message to [username].\n - Use command 'LOGOUT' to logout of chat server. ";
        display(msg);
        // Create data streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream((socket.getOutputStream()));
           // System.out.println("DEBUG: Data streams created");
        } catch (IOException e) {
            display("Exception in creating new streams");
            e.printStackTrace();
            return false;
        }

        try {
            //System.out.println("DEBUG: About to get usernames");
            String usernames = (String) sInput.readObject();
            System.out.println("Usernames being used at the moment are: " + usernames);
            Scanner userscan = new Scanner(System.in);
            while (usernames.contains(username)){
                System.out.println("Could you please choose a new username. This one has been taken.");
                System.out.print("New username: ");
                username = userscan.nextLine();
            }
            System.out.println("Username has been accepted");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Create thread to listen from server
        sl = new ServerListener();
        sl.start();



        // Send username to server as String
        try {
            sOutput.writeObject(username);
            System.out.println("Welcome to the server");
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
    public void endSL(){
    //Ends the server Listener thread (Which caused the infinite loop)
    sl.setT(false);}
    public void disconnect(){
        
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do
        System.exit(0);      //a bit of a cheat but this stops any errors from showing
    }
    //Send Message to server
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
       // System.out.println("DEBUG: Client created");
        // try to connect to server
        if (!client.start()){
           // System.out.println("DEBUG: Client did not start");
            return;
        }

        // get messages from the client
        Scanner scanner = new Scanner(System.in);
       // System.out.println("DEBUG: in scanner");
        //infinite loop getting messages
        while (true){
            System.out.print("> ");
            String msg = scanner.nextLine();
            // sends message to server that client wants to logout
            if (msg.equalsIgnoreCase("LOGOUT")){
                client.sendMessage(new Message(Message.LOGOUT, ""));
                client.endSL();
                break;
            } else if (msg.equalsIgnoreCase("PICTURE")){
                //TODO: send warning and picture to server
                client.sendMessage(new Message(Message.PICTURE, ""));
            } else if (msg.equalsIgnoreCase(("WHOISIN"))){
                client.sendMessage(new Message(Message.WHOISIN, ""));
            } else if (msg.startsWith(("KICK"))||msg.startsWith(("kick"))){
                String kicked = msg.substring(5);
                System.out.println("DEBUG: user to be kicked = " + kicked);
                client.sendMessage(new Message(Message.KICK, kicked));
            } else if(msg.startsWith(("DM"))||msg.startsWith(("dm"))){
				//DM:Kiar 
				String who = msg.substring(3);
				client.sendMessage(new Message(Message.DM, who));
				} else{
                client.sendMessage(new Message(Message.MESSAGE, msg));
            }
        }
        client.disconnect();
    }



    // Inner class that listens to new messages from the server
    class ServerListener extends Thread{
        boolean t = true;
        public void run(){
            while (t){
                try {
                    String msg = (String) sInput.readObject();
                    // TODO: add later
                    if (msg.equalsIgnoreCase("DISCONNECT")){
                        display("User disconnected");
                        disconnect();
                    } else {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    display("Connection to server is closed");
                    e.printStackTrace();
                    t=false;
                } catch (ClassNotFoundException e) {
                    //display("I am the issue");
                    e.printStackTrace();
                }
            }
        }
        public void setT(boolean n){
        t=n;}
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
