import java.io.*;
import java.net.*;

public class Client implements Runnable {

	private static Socket client = null;
	//send the data to the server socket
	private static ObjectOutputStream os = null;
	//read data from the ‘source_input’ Input Stream.
	private static ObjectInputStream is = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	public static void main(String[] args) {

		// the same as the server
		int portNumber = 2000;
		String host = "localhost";

		if (args.length < 2) {
			System.out.println("Default Server: " + host + ", Default Port: " + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
			System.out.println("Server: " + host + ", Port: " + portNumber);
		}

		//connect the socket
		try {
			client = new Socket(host, portNumber);

			//reads text from a character-input stream, 
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new ObjectOutputStream(client.getOutputStream());
			is = new ObjectInputStream(client.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unknown " + host);
		} catch (IOException e) {
			System.err.println("No Server found. ");
		}

		if (client != null && os != null && is != null) {
			try {
				//Create a thread to read from the server
				new Thread(new Client()).start();

				//if the socket is not closed
				while (!closed) {
					//read line
					String msg = (String) inputLine.readLine().trim();
					if ((msg.split(" ").length > 1))
					{
						os.writeObject(msg);
						os.flush();
					} else {
						os.writeObject(msg);
						os.flush();
					}
				}
				os.close();
				is.close();
				client.close();
			} catch (IOException e) 
			{
				System.err.println("IOException:  " + e);
			}
		}
	}

	//Create a thread to read from the server
	public void run() {
		String responseLine;
		try {
			while ((responseLine = (String) is.readObject()) != null)  {
				System.out.println(responseLine);
				
				//left the room 
				if (responseLine.indexOf("*** Bye") != -1)
					break;
			}
			closed = true;
			System.exit(0);
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Server Process Stopped Unexpectedly!!");
		}
	}
}