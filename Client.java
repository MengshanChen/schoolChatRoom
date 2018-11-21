import java.io.*;
import java.net.*;

public class Client implements Runnable{
    //use ObjectOutputStream/ObjectInputStream to implement Serialization
    public static Socket client = null;
    private static ObjectOutputStream os = null;
    private static ObjectInputStream is = null;
    private static BufferedReader input = null; 
    private static boolean closed = false; 

    public static void main(String[] args){
        int port = 2000;
        String host = "localHost";
        if(args.length < 2){
            System.out.println("Default Server: " + host + ", Default Port: " + port);
        }else{
            host = args[0];
            port = Integer.valueOf(args[1]).intValue();
        }

        try{
            System.out.println("Connecting to server on port " + port);
			client = new Socket(host, port);
			//must be "System.in" so that it can accept the server message
            input = new BufferedReader(new InputStreamReader(System.in));
            os = new ObjectOutputStream(client.getOutputStream());
            is = new ObjectInputStream(client.getInputStream());
            System.out.println("Connection made! Start reading..."); 
        }catch (UnknownHostException e) {
			System.err.println("Unknown " + host);
		} catch (IOException e) {
			System.err.println("No Server found. ");
        }

        //if the socket is not closed
        if(client != null && os != null && is != null){
            try{
                //give each thread a reference and start each thread by calling run
                new Thread(new Client()).start(); 
                while(!closed){
                    String msg = (String) input.readLine().trim(); 
                    //split the message into string array
                    if(msg.split(" ").length > 1){
                        os.writeObject(msg);
                        os.flush();
                    }else{
                        os.writeObject(msg);
                        os.flush(); 
                    }
                }
                os.close();
				is.close();
				client.close();
            }catch (IOException e){
				System.err.println("IOException:  " + e);
			}
        }
    }

	/**
	 * use start() to call the run method, 
	 * if it is not closed, it could read the message from BufferedReader input
	 * then write it into object = writeObject 
	 */
    public void run() {
        String response;
        try {
            while ((response = (String) is.readObject()) != null)  {
                System.out.println(response);
                if(response.indexOf("*** Bye") != -1){
                    break;  //server close
                }
            }
            closed = true;
            System.exit(0);

        } catch (IOException | ClassNotFoundException e) {
			System.err.println("Server Process Stopped Unexpectedly!!");
		}
     }
}