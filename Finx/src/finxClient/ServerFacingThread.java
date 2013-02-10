package finxClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFacingThread extends Thread{

	private Socket myClient;
	private String serverAddress = "127.0.0.1";
	private PrintStream protocolOutput;
	private BufferedReader protocolInput;
	private InputStreamReader protocolInputReader;
	private InetAddress ip;
	private StringBuilder MACaddr; 
	private Date lastPushDateTime;
	private Path FinxFolderPath; 
	
	private static final int NTHREDS = 20;
	
	private ExecutorService executor;


	public ServerFacingThread(String FinxFolderStringPath) {
		while (myClient == null) {
			try {
				sleep(5);
				connect_to_Server();
			} catch (InterruptedException e) { }
		}
		setOutputProtocolStream();
		setInputProtocolStream();
		setPath(FinxFolderStringPath);
		setExecutor();
		start();
	}

	public void run() {
		authenticate();
		receiveLastPushTime();
		walkFileTreeAndPush();
	}

	public void walkFileTreeAndPush() {
		try {
			Files.walkFileTree(FinxFolderPath, new FileTreeWalker(lastPushDateTime, this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String myFilePath) throws IOException {   
		// ask the Executor to allocate a Thread for sending the file
		sendTell("push:" + myFilePath);
		executor.execute(new FileWorker(myFilePath));
		// sendfile
		
		/*System.out.println("Path is: " + myFilePath);
		File myFile = new File (myFilePath);
		
		sendTell("push " + myFile.getName());
		
		byte [] mybytearray  = new byte [(int)myFile.length()];
		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(mybytearray,0,mybytearray.length);
		OutputStream os = myClient.getOutputStream();
		System.out.println("Sending...");
		os.write(mybytearray,0,mybytearray.length);
		os.flush();
		//myClient.close();
		System.out.println("Sent ! Or so it should be ...");*/
	}

	public void receiveLastPushTime() {
		// first send a request to the server
		sendRequest("lastpushtime");
		try {
			String dateTimeString = protocolInput.readLine();
			//dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
			myClient = new Socket(serverAddress, 9390);
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void setOutputProtocolStream() {
		try {
			protocolOutput = new PrintStream(myClient.getOutputStream());
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void setInputProtocolStream() {
		try {
			protocolInputReader = new InputStreamReader(myClient.getInputStream());
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
	
	public void setExecutor() {
		executor = Executors.newFixedThreadPool(NTHREDS);
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
	
	public Socket getSocket() {
		return myClient;
	}

}


