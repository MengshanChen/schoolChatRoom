import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Map;

/*
 * A school consulting service room 
 * For expert: there is a hashmap to store the list of experts with their id and password 
 * if expert joins, he/she will be asked for their id, password, and name
 * 
 * For student: if a student joins, he/she will be asked for id, email and name
 * Student will automatically get the school information if the setentce contains keyword 
 * Student can @name to send a private message
 * Student can @expert to call the consulting services, otherwise the message will send to all
 * 
 * server side: will see the list of Students by using object list
 * Messages will be stored in a file called record
 */

 public class Server{
     private static ServerSocket server = null;
     private static Socket client = null;
     public static ArrayList<clientThread> clients = new ArrayList<clientThread>();
     public static void main(String args[]){
        int port = 2000;
		if (args.length < 1){
			System.out.println("port number: " + port);
		} else {
			port = Integer.valueOf(args[0]).intValue();
			System.out.println("specific port number: " + port);
        }
        
        try{
            server = new ServerSocket(port); 
        } catch (IOException e) {
			System.out.println("Server Socket cannot be created");
        }
		
		/**
		 * accept() -> start() -> run() 
		 * accpet connection from the client if host is correct
		 * create a new object for current client then into list 
		 * this client will start by calling run() 
		 */
        int clientNum = 1;
        while (true) {
            try{
                client = server.accept(); //accept connection 
                clientThread curr = new clientThread(client, clients); 
                clients.add(curr); //add into arraylist
                curr.start(); //start the thread and call run() 
                System.out.println("User "  + clientNum + " is connected!");
                clientNum++;
            }catch (IOException e) {
                System.out.println("User could not be connected");
            }
        }
     }
 }

/**
 * create clientThread class for each client 
 * each client will execute run() method;
 * use ObjectInputStream in to read the input message
 * use ObjectOutputStream os to print the command
 * use synchronized to require all user to do the same thing
 * 
 */
class clientThread extends Thread {
    private String clientName = null;
    private ObjectInputStream in = null; //read
    private ObjectOutputStream os = null;  //write
    private Socket client = null; 
    private final ArrayList<clientThread> clients;
    
    public clientThread(Socket client, ArrayList<clientThread> clients){
        this.client = client;
        this.clients = clients;
    }

    //run() is inherited from Thread but needs to be overriden s
    public void run() {
		ArrayList<clientThread> clients = this.clients;
		HashMap<String, String> expertmap = new HashMap<>();
		expertmap.put ("2331021", "123");
		expertmap.put ("2331022", "456");
		expertmap.put ("2331023", "789");
		
		/***** create a user object ******/
		ArrayList<Student> studentList = new ArrayList<>();
		
		/***** write message into file ******/
		BufferedWriter writer = null;
		try{
			File file = new File("/Users/ericachen/Desktop/record.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));
			try {
				in = new ObjectInputStream(client.getInputStream());
				os = new ObjectOutputStream(client.getOutputStream());
	
				String name;
				String campusId;
				String email;
				String password;
				String type;
	
				while (true) {
                    
                    /**
                     * synchronize part pf methods; this = a monitor object 
                     * only one thread can execute inside block synchronized on the same object;
                     * only block the object, but no mutual exclusions;
                     * two threads visit differnt object, 
                     * will do their own things, no interference
                     * https://blog.csdn.net/zhangqiluGrubby/article/details/80500505
                     */
					synchronized(this)
					{
						/***** Id ******/
						//enter the id
						this.os.writeObject("Please enter your campus id :");
						this.os.flush();
						
						//read the id
						campusId = ((String) this.in.readObject()).trim();
	
						//expert? or student? 
						if(!expertmap.containsKey(campusId)){
							/***** Email ******/
							//enter the email
							this.os.writeObject("Please enter your school email:");
							this.os.flush();
	
							//read the email
							email = ((String) this.in.readObject()).trim();
							type = "Student";
						} else {
							/***** Password ******/
							//enter the password
							this.os.writeObject("Please enter your password:");
							this.os.flush();
	
							//read the password
							password = ((String) this.in.readObject()).trim();
	
							while(!password.equals(expertmap.get(campusId))){
								this.os.writeObject("Please enter your password:");
								this.os.flush();
								password = ((String) this.in.readObject()).trim();
							}
							type = "expert";
						}
						
						/***** Name ******/
						//enter the name
						this.os.writeObject("Please enter your name :");
						this.os.flush();
	
						//read the name
						name = ((String) this.in.readObject()).trim();
	
						//name exception
						if ((name.indexOf('@') == -1)) {
							break;
						} else {
							this.os.writeObject("Username should not start at '@'");
							this.os.flush();
						}
					}
				}
	
				//two different access priviledge: expert and general user
				if(type.equalsIgnoreCase("expert")){
					System.out.println("Expert connect"); 
				} else {
					System.out.println("User Name is " + name); 
					this.os.writeObject("*** Welcome " + name + " to our chat room ***\nEnter /quit to leave the chat room");
					this.os.writeObject("More school information: https://www.seattleu.edu");
					this.os.writeObject("More library information: https://www.seattleu.edu/library/");
					this.os.writeObject("More career information: https://www.seattleu.edu/careerengagement/");
					this.os.writeObject("@expert can provide private service");
					this.os.writeObject("Total people in the room: " + clients.size());
					this.os.flush();
				}
    
				synchronized(this)
				{
					for (clientThread current_client : clients)  
					{
						if (current_client != null && current_client == this) {
							clientName = "@" + name;
							break;
						}
					}
					for (clientThread current_client : clients) {
						if (current_client != null && current_client != this) {
							if(type.equals("expert")){
								current_client.os.writeObject("expert: " + name + " has joined");
							}else {
								current_client.os.writeObject("student: " + name + " has joined");
							}
							current_client.os.writeObject("Total people in the room: " + clients.size());
							current_client.os.flush();
						}
					}
				}
	
				//add student
				if(type.equals("student")){
					studentList.add(new Student(name, campusId));
					for(Student s : studentList){
						System.out.println(s.toString()); 
					}
				}
	
				/** Start the conversation never stop except "quit"*/
				while (true) {
					this.os.writeObject("Enter:");
					this.os.flush();
	
					//read the line and quit 
					String line = (String) in.readObject();
					if (line.startsWith("/quit")) {
						break;
					}
	
					//write into file
					writer.write("<" + name + "> " + line);
	
					/** key word respond */
					if(!type.equalsIgnoreCase("expert")){
						if(line.contains("school") || line.contains("campus")){
							this.os.writeObject("More school information: https://www.seattleu.edu");
						} else if (line.contains("library")) {
							this.os.writeObject("More library information: https://www.seattleu.edu/library/");
						} else if (line.contains("career")) {
							this.os.writeObject("More career information: https://www.seattleu.edu/careerengagement/");
						} else if (line.contains("@expert")){
							System.out.println("Calling an expert");
							this.os.writeObject("Thanks for your waiting. Expert is coming soon...");
							if (!clients.isEmpty()) {
								for (clientThread current_client : clients) {
									if (current_client != null && current_client != this && current_client.clientName == "expert") {
										special(line,"expert");  
									} 
								}
							}
						} 
					}
	
					//communication types
					if (line.startsWith("@")) {
						special(line,name); 
					} else {
						broad(line,name);
					}
				}
	
				//a user terminate the Session, remove the client
				this.os.writeObject("*** Bye! " + name + " ***");
				this.os.flush();
				System.out.println(name + " left.");
				clients.remove(this);
	
				synchronized(this) {
					if (!clients.isEmpty()) {
						for (clientThread curr : clients) {
							if (curr != null && curr != this && curr.clientName != null) {
								curr.os.writeObject("*** The user " + name + " left ***");
								curr.os.flush();
							}
						}
					}
				}
				this.in.close();
				this.os.close();
				client.close();
			} catch (IOException e) {
				System.out.println("User Session terminated");
	
			} catch (ClassNotFoundException e) {
				System.out.println("Class Not Found");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { 
			try{
				if(writer != null) writer.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}
    }
    
    /**
     * the message will be sent to everyone
     */
	void broad(String line, String name) throws IOException, ClassNotFoundException {
        synchronized(this){ //this = current user 
            for(clientThread curr : clients){
                if(curr != null && curr.clientName != null && curr.clientName != this.clientName){
                    curr.os.writeObject("<" + name + "> " + line); //this person sends message
                    curr.os.flush(); //write out  
                }
            }
            System.out.println("Message sent by " + this.clientName.substring(1));
        }
    }

    /**
     * the message will be sent to particular user 
     */
    void special(String line, String name) throws IOException, ClassNotFoundException {
        String[] words = line.split(" ", 2); 
        if(words.length > 1) { //input cannot be null
            words[1] = words[1].trim(); //remove space
            if(!words[1].isEmpty()){
                for(clientThread curr : clients){ //iterate the array to find the person
                    if(curr != null && curr != this && curr.clientName != null 
                        && curr.clientName.equals(words[0])){
                            //send the message to current client whose name is equal 
                            curr.os.writeObject("<" + name + "> " + words[1]);
                            curr.os.flush();
                            //System.out.println(this.clientName.substring(1) + " sent a private message to "+ current_client.clientName.substring(1));
                            //the person who send the message will be notified 
                            this.os.writeObject("Private Message sent to " + curr.clientName.substring(1));
                            this.os.flush();
                            break;

                    }
                }
            }

        }
    }
}

