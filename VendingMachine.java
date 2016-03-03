import java.io.*;
import java.util.*;
import java.net.*;
/*
Author: Metin Calis
Student Id: 21201640
Due Date: 13.03.2016

Programming Assignment 1
A Fixed Server Many Client VendingMachine Design
*/
public class VendingMachine implements Runnable{

	InetAddress ipAddress;
	int portNumber;
	
	ArrayList<Integer> item_id = new ArrayList<Integer>();
	ArrayList<String> item_name = new ArrayList<String>();
	ArrayList<Integer> item_count = new ArrayList<Integer>();
	ArrayList<Integer> item_count_temp = new ArrayList<Integer>();

	static ServerSocket welcomeSocket;
	static Socket connectionSocket;
	static Thread thread = null;

	boolean done = false;
	//Constructor for Client VendingMachine
	public VendingMachine(String arg0, String arg1) {
		try {
			ipAddress = InetAddress.getByName(arg0);
			portNumber = Integer.parseInt(arg1);
		} catch(UnknownHostException u){
			u.printStackTrace();
		}
	}
	//Constructor for Client VendingMachine
	public VendingMachine(String arg0) {
		portNumber = Integer.parseInt(arg0);
	}
	public static void main(String[] args){

		if (args.length == 1) {
			try{
				VendingMachine vendingMachineServer = new VendingMachine(args[0]);
				vendingMachineServer.readText();
				welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
				thread = new Thread(vendingMachineServer); 
				thread.start();
			} catch(IOException e){
				e.printStackTrace();
			}
		} else if (args.length == 2) {
			VendingMachine vendingMachineClient = new VendingMachine(args[0],args[1]);
			vendingMachineClient.clientLoop();
		} else {
			System.out.println("Enter a correct input");
		}
	}

	public void run(){
		String clientSentence;
		try {
			while(thread != null){
				System.out.println("item_list.txt is Read");
				System.out.println("The current list of items:");
				for (int i = 0; i<item_id.size();i++) {
					System.out.println("\t" + item_id.get(i) + " " + item_name.get(i) + " " + item_count_temp.get(i));
				}
				System.out.print("\r\nWaiting for a client ... "); 
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("A client is connected");
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				outToClient.writeBytes("Connection is Established\r\n"); 

				done = false;
				while(!done) {	
					try{
						clientSentence = null;
						outToClient.writeBytes("Choose a Message Type: GET ITEM (L)IST, (G)ET ITEM, (Q)UIT:\r\n");
						clientSentence = inFromClient.readLine();
						if(clientSentence.equals("L")){//Get Item List is Chosen
							System.out.println("The received message:");
							System.out.println("\tGET ITEM LIST");
							outToClient.writeBytes((Integer.toString(item_id.size()+1))+"\n"); //How many bytes to read
							System.out.println("Send the Message: ");
							System.out.println("\tITEM LIST");
							for (int i = 0; i<item_id.size();i++) {
								System.out.println("\t" + item_id.get(i) + " " + item_name.get(i) + " " + item_count_temp.get(i));
							}
							outToClient.writeBytes("ITEM LIST\r\n");
							for (int i = 0; i<item_id.size();i++) {
								outToClient.writeBytes(item_id.get(i) + " " + item_name.get(i) + " " + item_count_temp.get(i) + "\r\n");
							}
						} else if (clientSentence.equals("G")){//Get Item is Chosen
							System.out.println("The received message:");
							System.out.println("\tGET ITEM");
							outToClient.writeBytes("Give the Item id and Number of Items Seperated By a Space: \r\n");
							clientSentence = inFromClient.readLine();
							System.out.println("\t" + clientSentence);
							String[] line_split = clientSentence.split("\\s+");
							System.out.println("Send the message:");
							int index = item_id.indexOf(Integer.parseInt(line_split[0]));
							if (index == -1){ //Done to catch item ids that are not available
								System.out.println("\tOUT OF STOCK");
								outToClient.writeBytes("OUT OF STOCK\r\n");
							} else {
								if (item_count_temp.get(index) >= Integer.parseInt(line_split[1])){
									item_count_temp.set(index,(item_count_temp.get(index)-Integer.parseInt(line_split[1])));
									System.out.println("\tSUCCESS");
									outToClient.writeBytes("SUCCESS\r\n");
								}else {
									System.out.println("\tOUT OF STOCK");
									outToClient.writeBytes("OUT OF STOCK\r\n");
								}
							}	
						} else if (clientSentence.equals("Q")){//Quit is chosen
							System.out.println("The client has terminated the connection. ");
							System.out.println("The current list of items");
							for (int i = 0; i<item_id.size();i++) {
								System.out.println("\t" + item_id.get(i) +
										" " + item_name.get(i) + " " + Integer.toString(item_count_temp.get(i)));
							}
							for (int i = 0; i<item_id.size();i++) {
								outToClient.writeBytes(item_id.get(i) +
										" " + item_name.get(i) + " " + Integer.toString(item_count.get(i) - item_count_temp.get(i)) + "\r\n");
							}
							done = true;
							connectionSocket.close();

						}

					}catch(IOException ioe){
						System.out.println(ioe);
					}
				}
			}
		} catch (IOException e){
			System.out.println(e);
		}

	}
	public void clientLoop(){
		try{
			String userResponse = null;
			String serverResponse = null;
			BufferedReader inFromUser =
					new BufferedReader(new InputStreamReader(System.in));
			Socket clientSocket = new Socket(ipAddress, portNumber);
			DataOutputStream outToServer =
					new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer =
					new BufferedReader(new
							InputStreamReader(clientSocket.getInputStream()));
			serverResponse = inFromServer.readLine();
			System.out.println(serverResponse);

			while(true){
				
				try{
					serverResponse = null;
					userResponse = null;
					
					serverResponse = inFromServer.readLine();
					System.out.print(serverResponse + " ");
					
					userResponse = inFromUser.readLine();
					outToServer.writeBytes(userResponse+"\r\n");
					
					if(userResponse.equals("L")){
						System.out.println("The received message:");
						serverResponse = inFromServer.readLine(); //First how many items are available at items.txt is learned
						for (int i = 0;i<Integer.parseInt(serverResponse);i++){
							System.out.println("\t" + inFromServer.readLine());
						}
					} else if (userResponse.equals("G")){
						serverResponse = inFromServer.readLine();
						System.out.print(serverResponse);
						userResponse = inFromUser.readLine();
						outToServer.writeBytes(userResponse+"\r\n");
						System.out.println("The received message:");
						System.out.println("\t" + inFromServer.readLine());

					} else if (userResponse.equals("Q")){
						System.out.println("The summary of received items:");
						while ( (serverResponse = inFromServer.readLine()) != null) {
							System.out.println("\t" + serverResponse);
						}
						clientSocket.close();
						break;
					}
				} catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public void readText(){
		try{
			String path = "item_list.txt";
			FileInputStream fis = new FileInputStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while ( (line = reader.readLine()) != null) {
				String[] line_split = line.split("\\s+");
				item_id.add(Integer.parseInt(line_split[0]));
				item_name.add(line_split[1]);
				item_count.add(Integer.parseInt(line_split[2]));
				item_count_temp.add(Integer.parseInt(line_split[2]));
			}		
			reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
