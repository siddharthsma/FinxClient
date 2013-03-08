package finxClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class ServerFacingThread extends Thread{

	private Socket myClientProtocols;
	private Socket myClientFiles;
	private String serverAddress = "127.0.0.1";
	public PrintStream protocolOutput;
	public BufferedReader protocolInput;
	private InputStreamReader protocolInputReader;
	private InetAddress ip;
	private StringBuilder MACaddr; 
	private Date lastPushDateTime;
	private String FinxFolderStringPath;
	private Path FinxFolderPath; 

	private ServerCommandWatcher serverCommands;
	// Hashes that store
	public HashMap<String, File> push_map = new HashMap<String, File>();
	public HashSet<String> fetch_map = new HashSet<String>();
	
	public static final int BUFFER_SIZE = 100;  

	public ServerFacingThread(String FinxFolderStringPath) {
		this.FinxFolderStringPath = FinxFolderStringPath;
		while (myClientProtocols == null) {
			try {
				sleep(10000);
				connect_to_Server();
			} catch (InterruptedException e) { }
		}
		setOutputProtocolStream();
		setInputProtocolStream();
		setPath(FinxFolderStringPath);
		start();
	}

	public void run() {
		authenticate();
		receiveLastPushTime();
		walkFileTree();
		serverCommands = new ServerCommandWatcher(this);
		pushToServer();
		System.out.println("Managed to get here");
	}

	public void walkFileTree() {
		try {
			Files.walkFileTree(FinxFolderPath, new FileTreeWalker(lastPushDateTime, this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void dealWithFetchRequest(String relFilePath) {
		// Get the file name
		String[] pathSplit = relFilePath.split("/");
		String fileName = pathSplit[pathSplit.length-1];
		
		if (push_map.containsKey(fileName)) {
			/* we must deny fetch request since it would overwrite local
			 * changes
			 */
		}
		else {
			/* accept fetch request
			 */
			fetch_map.add(FinxFolderStringPath + relFilePath);
			protocolOutput.println("fetch#" + relFilePath);
			try {
				receiveFile(relFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	public void pushToServer() {
		Iterator pushIterator = push_map.keySet().iterator();
		while (pushIterator.hasNext()) {
	        try {
				sendFile(push_map.get(pushIterator.next()).getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public void sendFile(String myFilePath) throws IOException {   

		File myFile = new File(myFilePath);  
		String[] filePathSplit = myFile.getPath().split("FinxFolder/");
		
		//Send protocol push message with the file path
		protocolOutput.println("push#" + filePathSplit[1] );
		
		//Set Output Object Stream on the file transfer socket
		ObjectOutputStream oos = new ObjectOutputStream(myClientFiles.getOutputStream());  

		// Write the name of the file
		oos.writeObject(myFile.getName());  

		// Write the rest of the file
		FileInputStream fis = new FileInputStream(myFile);  
		byte [] buffer = new byte[BUFFER_SIZE];  
		Integer bytesRead = 0;  

		while ((bytesRead = fis.read(buffer)) > 0) {  
			oos.writeObject(bytesRead);  
			oos.writeObject(Arrays.copyOf(buffer, buffer.length));  
		}   
	}
	
	public void receiveFile(String relFilePath) throws Exception {

		// Set up Input Object Stream on File transfer socket  
		ObjectInputStream ois = new ObjectInputStream(myClientFiles.getInputStream()); 
		
		// Set up the FileOutput Stream and byte array buffer
		FileOutputStream fos = null;  
		byte [] buffer = new byte[BUFFER_SIZE];  

		// Create directories indicated in the relFilePath
		String[] directories = relFilePath.split("/");
		String relDirPath = "";
		for (int i=0; i < directories.length - 1; i++) {
			if (i==0) {
				relDirPath = directories[i];
			} else {
				relDirPath += "/" + directories[i];
			}
		}
		System.out.println(FinxFolderStringPath + relDirPath);
		File theFile = new File(FinxFolderStringPath + relDirPath);
		theFile.mkdirs();
		
		// 1. Read file name.  
		Object o = ois.readObject();  

		if (o instanceof String) {  
			fos = new FileOutputStream(FinxFolderStringPath + relFilePath);  
		} else {  
			throwException("Something is wrong");  
		}  

		// 2. Read file to the end.  
		Integer bytesRead = 0;  

		do {  
			o = ois.readObject();  

			if (!(o instanceof Integer)) {  
				throwException("Something is wrong");  
			}  

			bytesRead = (Integer)o;  

			o = ois.readObject();  

			if (!(o instanceof byte[])) {  
				throwException("Something is wrong");  
			}  

			buffer = (byte[])o;  

			// 3. Write data to output file.  
			fos.write(buffer, 0, bytesRead);  
		} while (bytesRead == BUFFER_SIZE);  
		
		fos.close();  
	}

	public void receiveLastPushTime() {
		// first send a request to the server
		sendRequest("lastpushtime");
		try {
			String dateTimeString = protocolInput.readLine();
			if (dateTimeString.equals("noTime")) {
				/* pass in a Date that is 0 milliseconds after 1st Jan 1970,
				 * thereby effectively forcing a Push
				 */
				lastPushDateTime = new Date(0);
			} else {
				lastPushDateTime = new Date(Integer.parseInt(dateTimeString));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		System.out.println("Push time set as: " + lastPushDateTime.toString());
	}

	public void authenticate() {
		/* Client sends his/her MAC address,
		 * and receives an authentication message from the Server */
		protocolOutput.println(getMACAddress());
		try {
			if(!protocolInput.readLine().equals("Authenticated")) {
				// dont need to worry about this yet
				System.out.println("Failed Authentication");
			} else { 
				System.out.println("Passed Authentication");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect_to_Server() {
		try {
			myClientProtocols = new Socket(serverAddress, 9390);
			myClientFiles = new Socket(serverAddress, 9391);
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void setOutputProtocolStream() {
		try {
			protocolOutput = new PrintStream(myClientProtocols.getOutputStream());
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void setInputProtocolStream() {
		try {
			protocolInputReader = new InputStreamReader(myClientProtocols.getInputStream());
			protocolInput = new BufferedReader(protocolInputReader);
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void sendRequest(String request) {
		protocolOutput.println(request);
	}

	public void sendTell(String tell) {
		protocolOutput.println(tell);
	}

	public String getMACAddress() {
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();

			MACaddr = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				MACaddr.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "" : ""));		
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e){
			e.printStackTrace();
		}
		return MACaddr.toString();
	}

	public void setPath(String Path) {
		FinxFolderPath = Paths.get(Path);
	}
	
	public static void throwException(String message) throws Exception {  
		throw new Exception(message);  
	}  


}


