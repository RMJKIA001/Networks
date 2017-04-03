import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Brigitte on 2017/04/02.
 *
 * This class manages the connections and client threads
 */
public class Server{
    // Unique ID for each connection
    private static int ID;
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
    public void start(){
        on = true;
        try {
            // Creates socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            //infinite loop to check for connections
            while(on){
                // Show that server is waiting
                display("Server is waiting for clients on port " + port + ".");

                // Accept connections
                Socket socket = serverSocket.accept();

                // Check if i should stop
                if (!on){
                    break;
                }

                // make a thread of the connection
                ClientThread t = new ClientThread(socket);
                // save the thread in the client list
                clients.add(t);
                t.start();
            }
            serverSocket.close();
            // possibly add try catches here so that error catching is easier
            for (int i=0; i<clients.size();++i){
                ClientThread tc = clients.get(i);
                //close stuff here
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        on = false;
        // doing this to move along in the infinite loop
        try {
            new Socket("localhost", port);
        }catch (Exception e){

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
        for (int i=0; i<clients.size();++i){
            ClientThread ct = clients.get(i);
            if (ct.id == id){
                clients.remove(i);
                return;
            }
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
        Server server = null;
        try {
            server = new Server(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();

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
