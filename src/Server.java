import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.awt.Point;

/**
 * Created by Brigitte on 2017/04/02.
 *
 * This class manages the connections and client threads
 */
public class Server{
    // Unique ID for each connection
    private static int uniqueID;
    // List of connected clients
    private ArrayList<ClientThread> clients;
    // So that we can display dates with our messages
    private SimpleDateFormat sdf;
    // port number that we listen for connection on
    private int port;
    // decides whether server will still run or not
    private boolean on;
    

    // Constructor for the server
    // Can edit this to include GUI if needed
    public Server (int port) throws IOException{
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        clients = new ArrayList<ClientThread>();
    }
    ServerSocket serverSocket;
    public void start(){
        on = true;
        try {
            // Creates socket used by the server
            serverSocket = new ServerSocket(port);

            //infinite loop to check for connections
            while(on){
                // Show that server is waiting
                display("Server is waiting for clients on port " + port + ".");
                // Accept connections
                Socket socket = serverSocket.accept();
               // System.out.println("DEBUG: Connection accepted");

                // Check if i should stop
                if (!on){
                    //System.out.println("DEBUG: Server stopped");
                    break;
                }

                // make a thread of the connection
                ClientThread t = new ClientThread(socket); 
                // save the thread in the client list
                clients.add(t);
                //System.out.println("DEBUG: Client thread created");
                t.start();
               // System.out.println("DEBUG: Client connection started");
            }
            serverSocket.close();
            // possibly add try catches here so that error catching is easier
            for (int i=0; i<clients.size();i++){//why do you use ++i
                ClientThread tc = clients.get(i);
                tc.sInput.close();
                tc.sOutput.close();
                tc.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dispays event (not a message) to the console
    public void display(String msg){
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }
    // Send a message to all clients
    private synchronized void broadcast (String message){
        // Formats and prints message to console
        String bcmessage = sdf.format(new Date()) + " " + message + "\n";
        System.out.println(bcmessage);

        // Sends the message to the clients
        // In reverse order in case we need to remove someone
        for (int i=clients.size(); --i>=0;){
            ClientThread ct = clients.get(i);
            // try to write to client. If a false boolean is returned then send failed,
            // and we remove the client
            if (!ct.writeMsg(bcmessage)){
                clients.remove(i);
                display(ct.username + " client disconnected");
            }
        }
    }
    // used for when the client logs off using LOGOUT message
    public synchronized void remove(int id){
    //System.out.println("Trying ");
        for (int i=0; i<clients.size();i++){
            ClientThread ct = clients.get(i);
            if (ct.id == id){
                clients.remove(i);
                break;
            }
        }
        if(clients.size()==0)
        {
          System.out.println("No more Clients");
          System.exit(0);
          //try{serverSocket.close();}
          //catch(Exception e){System.out.println("There is an error in your ways");}
        }
    }
    public static void main(String args[]){
        // start server on generic port unless a port number is specified
        int portNumber = 1500;
        switch(args.length){
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e){
                    System.out.println("Invalid port number");
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server {portNumber}");
                return;
        }
       
        Server server=null;
        try {
            server = new Server(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
       // System.out.println("DEBUG: Server started");

    }

    // Inner class for the thread. Easier this way than making a server object.
    public class ClientThread extends Thread {

        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        Message msg;
        String date;

        // Thread constructor
        public ClientThread(Socket socket){
            id = uniqueID++;
            this.socket = socket;

            try {
                //System.out.println("DEBUG: Creating data streams");
                sOutput = new ObjectOutputStream((socket.getOutputStream()));
                sInput = new ObjectInputStream(socket.getInputStream());
                //System.out.println("DEBUG: Data stream created");
                username = (String) sInput.readObject();
                display(username + " just connected.");
            } catch (IOException e) {
                e.printStackTrace();
                display("Exception in creating data streams " + e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            date = new Date().toString() + "\n";
        }
        // Loops until LOGOUT
        public void run(){
            boolean on = true;
            while(on){
                try {
                    msg = (Message) sInput.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
                String message = msg.getMessage();

                switch (msg.getType()){
                    case Message.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case Message.LOGOUT:
                        display(username + " disconnected from server");
                        on = false;
                        break;
                    case Message.PICTURE:
                        //TODO: how to save and display picture
                        String path="";
                        // Opens a file chooser window.
                        JFileChooser chooser = new JFileChooser();
                        // Creates a filter to allow only PNG and JPG images
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG and PNG Images", "jpg", "png");
                        chooser.setFileFilter(filter);//sets the filter
                        javax.swing.JFrame jf =new javax.swing.JFrame();
                        jf.setLocationRelativeTo(null);
                        int returnVal = chooser.showOpenDialog(jf);
                        // Upon clicking the approve button.
                        if (returnVal == JFileChooser.APPROVE_OPTION) { 
                           File file = new File(chooser.getSelectedFile().getPath());
                           path = file.getPath();
                        }
                        if (returnVal == JFileChooser.CANCEL_OPTION) {
                           path = "";
                        }  
                        display(path); 
                        break;
                    case Message.WHOISIN:
                        writeMsg("List of the users currently on the server at " +
                        sdf.format(new Date()) + "\n");
                        for (int i=0; i<clients.size();i++){
                            ClientThread ct = clients.get(i);
                            writeMsg((i+1) + ". " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // If here then client has logged out. So will remove client.
            remove(id);
            close();
        }
        private void close(){
            try {
                sOutput.close();
                sInput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private boolean writeMsg(String msg){
            if (!socket.isConnected()){
                close();
                return false;
            }
            // write message to the stream
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error sending message to " + username);
            }
            return true;
        }
    }

    //FIRST TRY:
/*
    public void run (){
        while (true){
            try {
                System.out.println("Waiting for client on port " + serversocket.getLocalPort() + "...");
                Socket server = serversocket.accept();

                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                DataInputStream dataInputStream = new DataInputStream(server.getInputStream());
                System.out.println(dataInputStream.readUTF());
                DataOutputStream dataOutputStream = new DataOutputStream((server.getOutputStream()));
                dataOutputStream.writeUTF("Thank you for connected to " + server.getLocalSocketAddress() + "\nGoodbye!");
                server.close();
            } catch (SocketTimeoutException s){
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }*/

}
