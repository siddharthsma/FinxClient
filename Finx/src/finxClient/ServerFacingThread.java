package finxClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFacingThread extends Thread{

	private Socket myClient;
	private String serverAddress = "127.0.0.1";
	private PrintStream protocolOutput;
	private BufferedReader protocolInput;
	private InputStreamReader protocolInputReader;
	private InetAddress ip;
	private StringBuilder MACaddr; 
	//private SimpleDateFormat dateFormat;
	private Date lastPushDateTime;
	private Path FinxFolderPath;
	private OutputStream os;
	private DataOutputStream dos;
	//private FileInputStream fis;
	//private BufferedInputStream bis;
	//private DataInputStream dis;
	
	
	public ServerFacingThread(String FinxFolderStringPath) {
		connect_to_Server();
		setOutputStream();
		setInputStream();
		setPath(FinxFolderStringPath);
		start();
	}
	
	public void run() {
		authenticate();
		receiveLastPushTime();
		setFileDeliveryStream();
		walkFileTreeAndPush();
		
	}
	
	public void walkFileTreeAndPush() {
		try {
			Files.walkFileTree(FinxFolderPath, new FileTreeWalker(lastPushDateTime, this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendFile(File myFile) {      
	    //Sending file name and file size to the server  
		 byte[] mybytearray = new byte[(int) myFile.length()];  
	     try {
			dos.writeUTF(myFile.getName());
			dos.writeLong(mybytearray.length);     
		    dos.write(mybytearray, 0, mybytearray.length);     
		    dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}         
	}
	
	public void receiveLastPushTime() {
		try {
			String dateTimeString = protocolInput.readLine();
			//dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (dateTimeString.equals("noTime")) {
				/* pass in a Date that is 0 milliseconds after 1st Jan 1970,
				 * thereby effectively forcing a Push
				 */
				lastPushDateTime = new Date(0);
				System.out.println(lastPushDateTime.toString());
			} else {
				lastPushDateTime = new Date(Integer.parseInt(dateTimeString));
				//lastPushDateTime = dateFormat.parse(dateTimeString);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void authenticate() {
		/* Client sends his/her MAC address,
		 * and receives an authentication message from the Server */
		protocolOutput.println(getMACAddress());
		try {
			if(!protocolInput.readLine().equals("Authenticated")) {
				// dont need to worry about this yet
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connect_to_Server() {
		try {
	           myClient = new Socket(serverAddress, 9396);
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	public void setOutputStream() {
		try {
			protocolOutput = new PrintStream(myClient.getOutputStream());
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	public void setInputStream() {
		try {
			protocolInputReader = new InputStreamReader(myClient.getInputStream());
			protocolInput = new BufferedReader(protocolInputReader);
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	public void setFileDeliveryStream() {
		 try {
				os = myClient.getOutputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}  
	     dos = new DataOutputStream(os);  
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
	

