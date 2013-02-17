package finxClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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



public class ServerFacingThread extends Thread{

	private Socket myClientProtocols;
	private Socket myClientFiles;
	private String serverAddress = "127.0.0.1";
	private PrintStream protocolOutput;
	private BufferedReader protocolInput;
	private InputStreamReader protocolInputReader;
	private InetAddress ip;
	private StringBuilder MACaddr; 
	private Date lastPushDateTime;
	private Path FinxFolderPath; 

	public static final int BUFFER_SIZE = 100;  

	public ServerFacingThread(String FinxFolderStringPath) {
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
		//walkFileTreeAndPush();
		try {
			sendFile("/Users/sameerambegaonkar/Desktop/FinxFolder/Programming.in.Objective-C.4th.Edition.pdf");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			sendFile("/Users/sameerambegaonkar/Desktop/FinxFolder/Previous statements.pdf");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Managed to get here");
	}

	public void walkFileTreeAndPush() {
		try {
			Files.walkFileTree(FinxFolderPath, new FileTreeWalker(lastPushDateTime, this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String myFilePath) throws IOException {   

		File myFile = new File(myFilePath);  
		String[] filePathSplit = myFilePath.split("FinxFolder/");
		//protocolOutput.println("push#" + filePathSplit[1] + "#" + Long.toString(myFile.length()) );
		protocolOutput.println("push#" + filePathSplit[1] );
		ObjectInputStream ois = new ObjectInputStream(myClientFiles.getInputStream());  
		ObjectOutputStream oos = new ObjectOutputStream(myClientFiles.getOutputStream());  

		oos.writeObject(myFile.getName());  

		FileInputStream fis = new FileInputStream(myFile);  
		byte [] buffer = new byte[BUFFER_SIZE];  
		Integer bytesRead = 0;  

		while ((bytesRead = fis.read(buffer)) > 0) {  
			oos.writeObject(bytesRead);  
			oos.writeObject(Arrays.copyOf(buffer, buffer.length));  
		}   
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


}


